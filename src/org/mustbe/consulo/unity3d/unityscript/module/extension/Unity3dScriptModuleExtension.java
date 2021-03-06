package org.mustbe.consulo.unity3d.unityscript.module.extension;

import org.consulo.module.extension.ModuleInheritableNamedPointer;
import org.consulo.module.extension.impl.ModuleExtensionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.javascript.lang.JavaScriptLanguage;
import org.mustbe.consulo.javascript.module.extension.JavaScriptModuleExtension;
import org.mustbe.consulo.unity3d.module.EmptyModuleInheritableNamedPointer;
import org.mustbe.consulo.unity3d.module.Unity3dModuleExtensionUtil;
import org.mustbe.consulo.unity3d.module.Unity3dRootModuleExtension;
import org.mustbe.consulo.unity3d.unityscript.lang.UnityScriptLanguageVersion;
import com.intellij.lang.LanguageVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 02.04.2015
 */
public class Unity3dScriptModuleExtension extends ModuleExtensionImpl<Unity3dScriptModuleExtension> implements
		JavaScriptModuleExtension<Unity3dScriptModuleExtension>
{
	public Unity3dScriptModuleExtension(@NotNull String id, @NotNull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
	}

	@NotNull
	@Override
	public ModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return EmptyModuleInheritableNamedPointer.empty();
	}

	@Nullable
	@Override
	@RequiredReadAction
	public Sdk getSdk()
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getSdk();
		}
		return null;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getSdkName()
	{
		Unity3dRootModuleExtension rootModuleExtension = Unity3dModuleExtensionUtil.getRootModuleExtension(getProject());
		if(rootModuleExtension != null)
		{
			return rootModuleExtension.getSdkName();
		}
		return null;
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		throw new UnsupportedOperationException("Use root module extension");
	}

	@NotNull
	@Override
	public LanguageVersion<JavaScriptLanguage> getLanguageVersion()
	{
		return UnityScriptLanguageVersion.getInstance();
	}
}
