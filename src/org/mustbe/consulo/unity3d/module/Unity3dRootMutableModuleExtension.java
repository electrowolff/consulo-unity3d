/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.unity3d.module;

import javax.swing.JComponent;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleMutableModuleExtension;
import org.mustbe.consulo.unity3d.module.ui.UnityConfigurationPanel;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.util.Comparing;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dRootMutableModuleExtension extends Unity3dRootModuleExtension implements DotNetSimpleMutableModuleExtension<Unity3dRootModuleExtension>
{
	public Unity3dRootMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	public void setBuildTarget(Unity3dTarget target)
	{
		myBuildTarget = target;
	}

	public void setFileName(@NotNull String name)
	{
		myFileName = name;
	}

	public void setOutputDir(@NotNull String dir)
	{
		myOutputDirectory = dir;
	}

	public void setNamespacePrefix(@Nullable String prefix)
	{
		myNamespacePrefix = prefix;
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	public JComponent createConfigurablePanel(@NotNull Runnable runnable)
	{
		return new UnityConfigurationPanel(this, getVariables(), runnable);
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@NotNull Unity3dRootModuleExtension ex)
	{
		return isModifiedImpl(ex) ||
				myBuildTarget != ex.getBuildTarget() ||
				!Comparing.equal(getFileName(), ex.getFileName()) ||
				!Comparing.equal(getNamespacePrefix(), ex.getNamespacePrefix()) ||
				!Comparing.equal(getOutputDir(), ex.getOutputDir());
	}
}
