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

package org.mustbe.consulo.unity3d.shaderlab.ide.completion;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.cgshader.CGLanguage;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderCGScript;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderLabFile;
import org.mustbe.consulo.unity3d.shaderlab.lang.psi.ShaderReference;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.impl.source.tree.injected.Place;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 11.10.2015
 */
public class ShaderLabCGCompletionContributor extends CompletionContributor
{
	public ShaderLabCGCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement().withLanguage(CGLanguage.INSTANCE), new CompletionProvider<CompletionParameters>()
		{
			@RequiredReadAction
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull final CompletionResultSet result)
			{
				Place shreds = InjectedLanguageUtil.getShreds(parameters.getOriginalFile());

				for(PsiLanguageInjectionHost.Shred shred : shreds)
				{
					PsiLanguageInjectionHost host = shred.getHost();
					if(host instanceof ShaderCGScript)
					{
						ShaderLabFile containingFile = (ShaderLabFile) host.getContainingFile();
						ShaderReference.consumeProperties(containingFile, new Consumer<LookupElement>()
						{
							@Override
							public void consume(LookupElement lookupElement)
							{
								result.addElement(lookupElement);
							}
						});
					}
				}
			}
		});
	}
}
