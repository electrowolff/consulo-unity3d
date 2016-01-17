package org.mustbe.consulo.unity3d.editor;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.unity3d.run.debugger.UnityProcessDialog;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.ProcessInfo;

/**
 * @author VISTALL
 * @since 17.01.2016
 */
@Logger
public class UnityEditorCommunication
{
	public static void request(@NotNull Project project, @NotNull Object postObject)
	{
		JavaSysMon javaSysMon = new JavaSysMon();
		ProcessInfo[] processInfos = javaSysMon.processTable();

		int pid = 0;
		for(ProcessInfo processInfo : processInfos)
		{
			String name = processInfo.getName();
			if(name.equalsIgnoreCase("unity.exe") || name.equalsIgnoreCase("unity") || name.equalsIgnoreCase("unity.app"))
			{
				pid = processInfo.getPid();
				break;
			}
		}
		if(pid == 0)
		{
			Messages.showErrorDialog(project, "UnityEditor is not opened", "Consulo");
			return;
		}

		int port = UnityProcessDialog.buildDebuggerPort(pid) + 2000;

		Gson gson = new Gson();
		String urlPart = postObject.getClass().getSimpleName();
		HttpPost post = new HttpPost("http://localhost:" + port + "/" + StringUtil.decapitalize(urlPart));
		post.setEntity(new StringEntity(gson.toJson(postObject), CharsetToolkit.UTF8_CHARSET));
		post.setHeader("Content-Type", "application/json");

		try
		{
			String data = HttpClients.createDefault().execute(post, new ResponseHandler<String>()
			{
				@Override
				public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException
				{
					return EntityUtils.toString(httpResponse.getEntity(), CharsetToolkit.UTF8_CHARSET);
				}
			});

			UnityEditorResponse unityEditorResponse = gson.fromJson(data, UnityEditorResponse.class);
			if(!unityEditorResponse.success)
			{
				Messages.showInfoMessage(project, "Unity cant execute this request", "Consulo");
			}
		}
		catch(IOException e)
		{
			LOGGER.error(e);

			Messages.showErrorDialog(project, "UnityEditor is not opened", "Consulo");
		}
	}
}