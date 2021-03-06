package org.mustbe.consulo.unity3d.jsonApi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.buildInWebServer.api.JsonPostRequestHandler;
import org.mustbe.consulo.unity3d.run.before.UnityRefreshQueue;

/**
 * @author VISTALL
 * @since 21.01.2016
 */
public class UnityRefreshResponseHandler extends JsonPostRequestHandler<UnityRefreshResponse>
{
	public UnityRefreshResponseHandler()
	{
		super("unityRefreshResponse", UnityRefreshResponse.class);
	}

	@NotNull
	@Override
	public JsonResponse handle(@NotNull UnityRefreshResponse unityRefreshResponse)
	{
		UnityRefreshQueue.refreshReceived(unityRefreshResponse.uuid);
		return JsonResponse.asSuccess(null);
	}
}
