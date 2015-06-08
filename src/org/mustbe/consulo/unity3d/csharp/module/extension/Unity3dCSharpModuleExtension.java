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

package org.mustbe.consulo.unity3d.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpSimpleModuleExtension;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.openapi.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class Unity3dCSharpModuleExtension extends BaseCSharpSimpleModuleExtension<Unity3dCSharpModuleExtension>
{
	public Unity3dCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getAssemblyTitle()
	{
		return getModule().getName();
	}

	@NotNull
	@Override
	public CSharpLanguageVersion getLanguageVersion()
	{
		return CSharpLanguageVersion._4_0;
	}
}
