package org.mustbe.consulo.unity3d.run.before;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.editor.UnityEditorCommunication;
import org.mustbe.consulo.unity3d.editor.UnityRefresh;
import org.mustbe.consulo.unity3d.run.test.Unity3dTestConfiguration;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.ui.UIUtil;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class UnityRefreshBeforeRunTaskProvider extends BeforeRunTaskProvider<UnityRefreshBeforeRunTask>
{
	private static final Key<UnityRefreshBeforeRunTask> ourKey = Key.create("unity.refresh.task");

	@Override
	public Key<UnityRefreshBeforeRunTask> getId()
	{
		return ourKey;
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return Unity3dIcons.Unity3d;
	}

	@Nullable
	@Override
	public Icon getTaskIcon(UnityRefreshBeforeRunTask task)
	{
		return Unity3dIcons.Unity3d;
	}

	@Override
	public String getName()
	{
		return "UnityEditor refresh";
	}

	@Override
	public String getDescription(UnityRefreshBeforeRunTask task)
	{
		return getName();
	}

	@Override
	public boolean isConfigurable()
	{
		return false;
	}

	@Nullable
	@Override
	public UnityRefreshBeforeRunTask createTask(RunConfiguration runConfiguration)
	{
		return runConfiguration instanceof Unity3dTestConfiguration ? new UnityRefreshBeforeRunTask(ourKey) : null;
	}

	@Override
	public boolean configureTask(RunConfiguration runConfiguration, UnityRefreshBeforeRunTask task)
	{
		return false;
	}

	@Override
	public boolean canExecuteTask(RunConfiguration configuration, UnityRefreshBeforeRunTask task)
	{
		return true;
	}

	@Override
	public boolean executeTask(DataContext context, RunConfiguration configuration, final ExecutionEnvironment env, UnityRefreshBeforeRunTask task)
	{
		final Semaphore done = new Semaphore();
		done.down();
		final Ref<Boolean> ref = Ref.create();

		UIUtil.invokeLaterIfNeeded(new Runnable()
		{
			@Override
			public void run()
			{
				FileDocumentManager.getInstance().saveAllDocuments();

				new Task.Backgroundable(env.getProject(), "Queue UnityEditor refresh", true)
				{
					private boolean myReceiveData;
					private AccessToken myAccessToken;

					@Override
					public void run(@NotNull ProgressIndicator indicator)
					{
						UnityRefresh postObject = new UnityRefresh();
						myAccessToken = UnityRefreshQueue.wantRefresh(postObject.uuid, new Runnable()
						{
							@Override
							public void run()
							{
								ref.set(Boolean.TRUE);
								myReceiveData = true;
							}
						});

						boolean request = UnityEditorCommunication.request(env.getProject(), postObject, true);
						if(!request)
						{
							new Notification("unity", ApplicationNamesInfo.getInstance().getProductName(), "UnityEditor is not responding", NotificationType.WARNING).notify(env.getProject());

							myAccessToken.finish();
							ref.set(Boolean.FALSE);
							done.up();
							return;
						}

						while(!myReceiveData)
						{
							if(indicator.isCanceled())
							{
								myAccessToken.finish();
								ref.set(Boolean.FALSE);
								break;
							}

							TimeoutUtil.sleep(500L);
						}

						done.up();
					}
				}.queue();
			}
		});

		done.waitFor();
		return ref.get() == Boolean.TRUE;
	}
}
