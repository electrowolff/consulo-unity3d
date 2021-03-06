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

package org.mustbe.consulo.unity3d.shaderlab.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 08.05.2015
 */
public class ShaderPropertyAttribute extends ShaderLabElement
{
	public ShaderPropertyAttribute(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public ShaderReference getReferenceExpression()
	{
		return findChildByClass(ShaderReference.class);
	}

	@Override
	public void accept(SharpLabElementVisitor visitor)
	{
		visitor.visitPropertyAttribute(this);
	}
}
