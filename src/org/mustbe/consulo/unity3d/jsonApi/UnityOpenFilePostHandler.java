/*
 * Copyright 2013-2015 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.unity3d.jsonApi;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.buildInWebServer.api.RequestFocusHttpRequestHandler;
import org.mustbe.consulo.unity3d.bundle.Unity3dBundleType;
import consulo.unity3d.projectImport.Unity3dProjectImportBuilder;
import consulo.unity3d.projectImport.Unity3dProjectImportProvider;
import com.intellij.ide.actions.ImportModuleAction;
import com.intellij.ide.util.newProjectWizard.AddModuleWizard;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;

/**
 * @author VISTALL
 * @since 14.11.2015
 */
public class UnityOpenFilePostHandler extends JsonPostRequestHandler<UnityOpenFilePostHandlerRequest>
{
	private static final Set<String> ourSupportedContentTypes = ContainerUtil.newHashSet("UnityEditor.MonoScript", "UnityEngine.Shader");

	public UnityOpenFilePostHandler()
	{
		super("unityOpenFile", UnityOpenFilePostHandlerRequest.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull final UnityOpenFilePostHandlerRequest body)
	{
		String contentType = body.contentType;
		if(!ourSupportedContentTypes.contains(contentType))
		{
			return JsonResponse.asError("unsupported-content-type");
		}

		UIUtil.invokeLaterIfNeeded(new Runnable()
		{
			@Override
			public void run()
			{
				VirtualFile projectVirtualFile = LocalFileSystem.getInstance().findFileByPath(body.projectPath);
				if(projectVirtualFile != null)
				{
					Project openedProject = null;
					Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
					for(Project openProject : openProjects)
					{
						if(projectVirtualFile.equals(openProject.getBaseDir()))
						{
							openedProject = openProject;
							break;
						}
					}

					if(openedProject == null)
					{
						if(!new File(projectVirtualFile.getPath(), Project.DIRECTORY_STORE_FOLDER).exists())
						{
							String sdkPath = SystemInfo.isMac ? body.editorPath : new File(body.editorPath).getParentFile().getParentFile().getPath();

							VirtualFile sdkFileHome = LocalFileSystem.getInstance().findFileByPath(sdkPath);
							if(sdkFileHome ==null)
							{
								RequestFocusHttpRequestHandler.activateFrame(WindowManager.getInstance().findVisibleFrame());
								Messages.showErrorDialog("Unity path is not resolved: " + sdkPath, "Consulo");
								return;
							}

							Sdk targetSdk = null;
							List<Sdk> sdksOfType = SdkTable.getInstance().getSdksOfType(Unity3dBundleType.getInstance());
							for(Sdk sdk : sdksOfType)
							{
								VirtualFile homeDirectory = sdk.getHomeDirectory();
								if(sdkFileHome.equals(homeDirectory))
								{
									targetSdk = sdk;
									break;
								}
							}

							if(targetSdk == null)
							{
								targetSdk = SdkConfigurationUtil.createAndAddSDK(sdkPath, Unity3dBundleType.getInstance(), false);
							}

							if(targetSdk ==null)
							{
								RequestFocusHttpRequestHandler.activateFrame(WindowManager.getInstance().findVisibleFrame());
								Messages.showErrorDialog("Unity SDK cant add by path: " + sdkPath, "Consulo");
								return;
							}

							Unity3dProjectImportProvider importProvider = new Unity3dProjectImportProvider();
							Unity3dProjectImportBuilder builder = (Unity3dProjectImportBuilder) importProvider.getBuilder();
							builder.setUnitySdk(targetSdk);

							AddModuleWizard wizard = ImportModuleAction.createImportWizard(null, null, projectVirtualFile, importProvider);
							if(wizard == null)
							{
								return;
							}

							List<Module> fromWizard = ImportModuleAction.createFromWizard(null, wizard);
							if(fromWizard.isEmpty())
							{
								return;
							}

							wizard.close(DialogWrapper.OK_EXIT_CODE);

							final Project temp = fromWizard.get(0).getProject();
							activateFrame(temp, body);
							StartupManager.getInstance(temp).registerPostStartupActivity(new Runnable()
							{
								@Override
								public void run()
								{
									openFile(temp, body);
								}
							});
						}
						else
						{
							try
							{
								openedProject = ProjectManager.getInstance().loadAndOpenProject(projectVirtualFile.getPath());
							}
							catch(Exception e)
							{
								Messages.showErrorDialog("Fail to open project by path: " + projectVirtualFile.getPath(), "Consulo");
							}

							activateFrame(openedProject, body);
							openFile(openedProject, body);
						}
					}
					else
					{
						activateFrame(openedProject, body);
						openFile(openedProject, body);
					}
				}
			}
		});
		return JsonResponse.asSuccess(null);
	}

	private void activateFrame(@Nullable Project openedProject, @NotNull UnityOpenFilePostHandlerRequest body)
	{
		if(openedProject == null)
		{
			return;
		}

		IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(openedProject);
		RequestFocusHttpRequestHandler.activateFrame(ideFrame);
	}

	private void openFile(@Nullable Project openedProject, @NotNull UnityOpenFilePostHandlerRequest body)
	{
		if(openedProject == null)
		{
			return;
		}

		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(body.filePath);
		if(fileByPath != null)
		{
			OpenFileDescriptor descriptor = new OpenFileDescriptor(openedProject, fileByPath, body.line, -1);
			FileEditorManager.getInstance(openedProject).openTextEditor(descriptor, true);
		}
	}
}
