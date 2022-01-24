package com.msp.board_service.common.customValidation

import com.msp.board_service.config.LangCodeConfig
import javax.validation.ConstraintValidator
import com.msp.board_service.domain.MultiLang
import com.msp.board_service.exception.CustomException
import java.util.ArrayList
import javax.validation.ConstraintValidatorContext

class TitleValueValidator : ConstraintValidator<TitleValidation?, ArrayList<MultiLang>?> {

    override fun initialize(constraintAnnotation: TitleValidation?) {
        super.initialize(constraintAnnotation)
    }

    override fun isValid(title: ArrayList<MultiLang>?, context: ConstraintValidatorContext): Boolean {
        if (title == null || title.isEmpty()) {
            addConstraintViolation(context, "제목을 입력하세요.")
            return false
        }
        for ((lang, value) in title) {
            if (!LangCodeConfig.LANG_CODE_SET.contains(lang.toLowerCase())){
                addConstraintViolation(context, "언어 코드를 확인해주세요")
                return false
            }else if (value.length < 5 || value.length > 50){
                addConstraintViolation(context, "길이가 5에서 50 사이여야 합니다(공백포함)")
                return false
            }
        }
        return true
    }


    private fun addConstraintViolation(context: ConstraintValidatorContext, msg: String) {
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate(msg).addConstraintViolation()
    }


    /**
     * 게시글 수정시 Optional Field
     */
    fun isValid(title: ArrayList<MultiLang>) {
        if (title.isEmpty()) {
            throw CustomException.validation(message = "제목을 입력하세요.",field = "title")
        }
        for ((lang, value) in title) {
            if (!LangCodeConfig.LANG_CODE_SET.contains(lang.toLowerCase())){
                throw CustomException.validation(message = "언어 코드를 확인해주세요",field = "title")
            }else if (value.length < 5 || value.length > 50){
                throw CustomException.validation(message = "길이가 5에서 50 사이여야 합니다(공백포함)",field = "title")
            }
        }
    }

    companion object{
        fun titleValidation(title: ArrayList<MultiLang>) = TitleValueValidator().isValid(title)
    }
}