/*
 * Copyright 2013-2016 must-be.org
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

package consulo.unity3d.jsonApi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerMessage;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.problems.Problem;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.util.ObjectUtil;
import com.intellij.util.PairConsumer;
import com.intellij.util.ui.MessageCategory;
import com.intellij.util.ui.UIUtil;
import consulo.unity3d.UnityConsoleService;

/**
 * @author VISTALL
 * @since 07-Jun-16
 */
public class UnityLogPostHandler extends JsonPostRequestHandler<UnityLogPostHandlerRequest>
{
	private static Map<String, Integer> ourTypeMap = new HashMap<String, Integer>();

	static
	{
		ourTypeMap.put("Error", MessageCategory.ERROR);
		ourTypeMap.put("Assert", MessageCategory.ERROR);
		ourTypeMap.put("Warning", MessageCategory.WARNING);
		ourTypeMap.put("Log", MessageCategory.INFORMATION);
		ourTypeMap.put("Exception", MessageCategory.ERROR);
	}

	public UnityLogPostHandler()
	{
		super("unityLog", UnityLogPostHandlerRequest.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull final UnityLogPostHandlerRequest request)
	{
		UIUtil.invokeLaterIfNeeded(new Runnable()
		{
			@Override
			public void run()
			{
				UnityConsoleService.byPath(request.projectPath, new PairConsumer<Project, NewErrorTreeViewPanel>()
				{
					@Override
					@RequiredDispatchThread
					public void consume(Project project, NewErrorTreeViewPanel panel)
					{
						int value = ObjectUtil.notNull(ourTypeMap.get(request.type), MessageCategory.INFORMATION);

						DotNetCompilerMessage message = UnityLogParser.extractFileInfo(project, request.condition);

						if(message != null)
						{
							VirtualFile fileByUrl = message.getFileUrl() == null ? null : VirtualFileManager.getInstance().findFileByUrl(message.getFileUrl());
							if(fileByUrl != null && value == MessageCategory.ERROR)
							{
								Problem problem = WolfTheProblemSolver.getInstance(project).convertToProblem(fileByUrl, message.getLine(), message.getColumn(), new String[]{message.getMessage()});
								if(problem != null)
								{
									WolfTheProblemSolver.getInstance(project).reportProblems(fileByUrl, Arrays.<Problem>asList(problem));
								}
							}

							panel.addMessage(value, new String[]{message.getMessage()}, fileByUrl, message.getLine() - 1, message.getColumn(), null);
						}
						else
						{
							panel.addMessage(value, new String[]{request.condition, request.stackTrace}, null, -1, -1, null);
						}
					}
				});
			}
		});

		return JsonResponse.asSuccess(null);
	}
}
