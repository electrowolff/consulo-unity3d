package org.mustbe.consulo.unity3d.unityscript;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.unity3d.Unity3dIcons;
import org.mustbe.consulo.unity3d.unityscript.lang.impl.csharp.UnityScriptToNativeElementTransformer;
import org.mustbe.consulo.unity3d.unityscript.module.extension.Unity3dScriptModuleExtension;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptor;
import com.intellij.ide.IconDescriptorUpdater;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.util.BitUtil;

/**
 * @author VISTALL
 * @since 19.07.2015
 */
public class UnityScriptIconDescriptorUpdater implements IconDescriptorUpdater
{
	@Override
	@RequiredReadAction
	public void updateIcon(@NotNull IconDescriptor iconDescriptor, @NotNull PsiElement element, int flags)
	{
		if(element instanceof JSFile)
		{
			Unity3dScriptModuleExtension moduleExtension = ModuleUtilCore.getExtension(element, Unity3dScriptModuleExtension.class);
			if(moduleExtension == null)
			{
				return;
			}

			iconDescriptor.setMainIcon(AllIcons.Nodes.Class);
			iconDescriptor.addLayerIcon(Unity3dIcons.Js);
			if(BitUtil.isSet(flags, Iconable.ICON_FLAG_VISIBILITY))
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_public);
			}
		}
		else if(element instanceof CSharpLightTypeDeclarationBuilder)
		{
			if(element.getUserData(UnityScriptToNativeElementTransformer.JS_MARKER) == Boolean.TRUE)
			{
				PsiElement navigationElement = element.getNavigationElement();
				assert navigationElement != null;

				updateIcon(iconDescriptor, navigationElement, flags);
			}
		}
	}
}
