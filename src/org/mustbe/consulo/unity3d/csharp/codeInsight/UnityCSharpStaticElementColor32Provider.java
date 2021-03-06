package org.mustbe.consulo.unity3d.csharp.codeInsight;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.unity3d.Unity3dTypes;
import com.intellij.openapi.editor.ElementColorProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 01.04.2015
 */
@SuppressWarnings("UseJBColor")
public class UnityCSharpStaticElementColor32Provider implements ElementColorProvider
{
	@Nullable
	@Override
	@RequiredReadAction
	public Color getColorFrom(@NotNull PsiElement element)
	{
		IElementType elementType = element.getNode().getElementType();
		if(elementType == CSharpTokens.NEW_KEYWORD)
		{
			PsiElement parent = element.getParent();
			if(!(parent instanceof CSharpNewExpression))
			{
				return null;
			}

			PsiElement resolvedElementMaybeConstructor = ((CSharpNewExpression) parent).resolveToCallable();
			if(!(resolvedElementMaybeConstructor instanceof CSharpConstructorDeclaration))
			{
				return null;
			}

			DotNetType newType = ((CSharpNewExpression) parent).getNewType();
			if(newType == null)
			{
				return null;
			}

			if(UnityCSharpStaticElementColorProvider.parentIsColorType(resolvedElementMaybeConstructor,
					Unity3dTypes.UnityEngine.Color32))
			{
				ResolveResult validResult = CSharpResolveUtil.findFirstValidResult(((CSharpNewExpression) parent)
						.multiResolve(false));
				if(!(validResult instanceof MethodResolveResult))
				{
					return null;
				}

				MethodCalcResult calcResult = ((MethodResolveResult) validResult).getCalcResult();
				Map<String, Integer> map = new HashMap<String, Integer>(4);
				for(NCallArgument nCallArgument : calcResult.getArguments())
				{
					String parameterName = nCallArgument.getParameterName();
					if(parameterName == null)
					{
						continue;
					}
					CSharpCallArgument callArgument = nCallArgument.getCallArgument();
					if(callArgument == null)
					{
						continue;
					}
					DotNetExpression argumentExpression = callArgument.getArgumentExpression();
					if(argumentExpression == null)
					{
						continue;
					}

					Object value = new ConstantExpressionEvaluator(argumentExpression).getValue();
					if(value instanceof Number)
					{
						int intValue = ((Number) value).intValue();
						if(intValue < 0 || intValue > 255)
						{
							return null;
						}
						map.put(parameterName, intValue);
					}
					else
					{
						return null;
					}
				}

				if(map.size() == 4)
				{
					return new Color(map.get("r"), map.get("g"), map.get("b"), map.get("a"));
				}
			}
		}

		return null;
	}

	@Override
	@RequiredWriteAction
	public void setColorTo(@NotNull PsiElement element, @NotNull Color color)
	{
		CSharpNewExpression newExpression = PsiTreeUtil.getParentOfType(element, CSharpNewExpression.class);
		assert newExpression != null;
		DotNetType newType = newExpression.getNewType();
		assert newType != null;
		StringBuilder builder = new StringBuilder().append("new ").append(newType.getText()).append("(");
		builder.append(color.getRed()).append(", ");
		builder.append(color.getGreen()).append(", ");
		builder.append(color.getBlue()).append(", ");
		builder.append(color.getAlpha());
		builder.append(")");

		CSharpNewExpression expression = (CSharpNewExpression) CSharpFileFactory.createExpression(element.getProject()
				, builder.toString());

		newExpression.getParameterList().replace(expression.getParameterList());
	}
}
