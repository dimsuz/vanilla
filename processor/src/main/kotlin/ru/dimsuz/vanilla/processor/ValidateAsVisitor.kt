package ru.dimsuz.vanilla.processor

import com.github.michaelbull.result.Result
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid

internal class ValidateAsVisitor(
    private val onMatch: (Result<ModelPair, Error>) -> Unit
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        onMatch(findValidationModelPairs(classDeclaration))
    }
}
