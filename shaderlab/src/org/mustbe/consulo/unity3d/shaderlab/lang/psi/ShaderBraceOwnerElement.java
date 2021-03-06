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
import org.mustbe.consulo.unity3d.shaderlab.lang.parser.roles.ShaderLabRole;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.05.2015
 */
public abstract class ShaderBraceOwnerElement extends ShaderLabElement implements ShaderBraceOwner, ShaderRoleOwner
{
	public ShaderBraceOwnerElement(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	@Nullable
	public ShaderLabRole getRole()
	{
		PsiElement element = findNotNullChildByType(ShaderLabKeyTokens.START_KEYWORD);
		return ShaderLabRole.findRole(element.getText());
	}

	@Override
	@Nullable
	public PsiElement getLeftBrace()
	{
		return findChildByType(ShaderLabTokens.LBRACE);
	}

	@Override
	@Nullable
	public PsiElement getRightBrace()
	{
		return findChildByType(ShaderLabTokens.RBRACE);
	}
}
