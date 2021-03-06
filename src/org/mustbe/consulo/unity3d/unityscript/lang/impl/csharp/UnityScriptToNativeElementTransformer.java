package org.mustbe.consulo.unity3d.unityscript.lang.impl.csharp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.ToNativeElementTransformer;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.javascript.lang.JavaScriptLanguage;
import org.mustbe.consulo.unity3d.Unity3dTypes;
import org.mustbe.consulo.unity3d.unityscript.lang.impl.UnityScriptDotNetTypeDeclaration;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSSourceElement;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 18.12.2015
 */
public class UnityScriptToNativeElementTransformer implements ToNativeElementTransformer
{
	public static final Key<Boolean> JS_MARKER = Key.create("unity.script.marker");

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement transform(@NotNull PsiElement psiElement)
	{
		if(psiElement instanceof UnityScriptDotNetTypeDeclaration)
		{
			CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(psiElement)
			{
				@NotNull
				@Override
				public Language getLanguage()
				{
					return JavaScriptLanguage.INSTANCE;
				}
			};
			PsiElement navigationElement = psiElement.getNavigationElement();

			builder.withName(((UnityScriptDotNetTypeDeclaration) psiElement).getName());
			builder.setNavigationElement(navigationElement);
			builder.addModifier(DotNetModifier.PUBLIC);
			builder.putUserData(JS_MARKER, Boolean.TRUE);
			builder.addExtendType(new CSharpTypeRefByQName(psiElement, Unity3dTypes.UnityEngine.MonoBehaviour));

			if(navigationElement instanceof JSFile)
			{
				for(JSSourceElement jsSourceElement : ((JSFile) navigationElement).getStatements())
				{
					if(jsSourceElement instanceof JSFunction)
					{
						String funcName = jsSourceElement.getName();
						if(funcName == null)
						{
							continue;
						}
						CSharpLightMethodDeclarationBuilder methodDeclarationBuilder = new CSharpLightMethodDeclarationBuilder(psiElement.getProject());
						methodDeclarationBuilder.addModifier(DotNetModifier.PUBLIC);
						methodDeclarationBuilder.withReturnType(new CSharpTypeRefByQName(psiElement, DotNetTypes.System.Void));
						methodDeclarationBuilder.withName(funcName);
						methodDeclarationBuilder.setNavigationElement(jsSourceElement);
						builder.addMember(methodDeclarationBuilder);
					}
				}
			}
			return builder;
		}
		return null;
	}
}
