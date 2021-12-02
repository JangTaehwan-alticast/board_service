package com.msp.board_service.util

import com.msp.board_service.exception.CustomException
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.util.StringUtils
import java.util.*

class MakeWhereCriteria {

    companion object{

        fun makeWhereCriteria(param:String, exp: String, value: String, valueType: String):Criteria{
            return when(exp){
                "eq" ->{
                    when(valueType){
                        "string"->{
                            if(value == "null"){
                                Criteria.where(param).`in`(null,"","null")
                            }else{
                                Criteria.where(param).`is`(value)
                            }
                        }
                        "int"-> {
                            if (value == "null") {
                                Criteria.where(param).`in`(null)
                            } else {
                                Criteria.where(param).`is`(value.toInt())
                            }
                        }
                        "long"->{
                            if (value == "null") {
                                Criteria.where(param).`in`(null)
                            } else {
                                Criteria.where(param).`is`(value.toLong())
                            }
                        }
                        "float"->{
                            if (value == "null") {
                                Criteria.where(param).`in`(null)
                            } else {
                                Criteria.where(param).`is`(value.toFloat())
                            }
                        }
                        "double"->{
                            if (value == "null") {
                                Criteria.where(param).`in`(null)
                            } else {
                                Criteria.where(param).`is`(value.toDouble())
                            }
                        }
                        else -> Criteria()
                    }

                }
                "ne" ->{
                    when(valueType){
                        "string"->{
                            if(value == "null"){
                                Criteria.where(param).`nin`(null,"","null")
                            }else{
                                Criteria.where(param).`ne`(value)
                            }
                        }
                        "int"-> {
                            if (value == "null") {
                                Criteria.where(param).`ne`(null)
                            } else {
                                Criteria.where(param).`ne`(value.toInt())
                            }
                        }
                        "long"->{
                            if (value == "null") {
                                Criteria.where(param).`ne`(null)
                            } else {
                                Criteria.where(param).`ne`(value.toLong())
                            }
                        }
                        "float"->{
                            if (value == "null") {
                                Criteria.where(param).`ne`(null)
                            } else {
                                Criteria.where(param).`ne`(value.toFloat())
                            }
                        }
                        "double"->{
                            if (value == "null") {
                                Criteria.where(param).`ne`(null)
                            } else {
                                Criteria.where(param).`ne`(value.toDouble())
                            }
                        }
                        else -> Criteria()
                    }
                }
                "lt" ->{
                    when(valueType){
                        "string"->{
                            Criteria.where(param).`lt`(value)
                        }
                        "int"-> {
                            Criteria.where(param).`lt`(value.toInt())
                        }
                        "long"->{
                            Criteria.where(param).`lt`(value.toLong())
                        }
                        "float"->{
                            Criteria.where(param).`lt`(value.toFloat())
                        }
                        "double"->{
                            Criteria.where(param).`lt`(value.toDouble())
                        }
                        else -> Criteria()
                    }
                }
                "le" ->{
                    when(valueType){
                        "string"->{
                            Criteria.where(param).`lte`(value)
                        }
                        "int"-> {
                            Criteria.where(param).`lte`(value.toInt())
                        }
                        "long"->{
                            Criteria.where(param).`lte`(value.toLong())
                        }
                        "float"->{
                            Criteria.where(param).`lte`(value.toFloat())
                        }
                        "double"->{
                            Criteria.where(param).`lte`(value.toDouble())
                        }
                        else -> Criteria()
                    }
                }
                "gt" ->{
                    when(valueType){
                        "string"->{
                            Criteria.where(param).`gt`(value)
                        }
                        "int"-> {
                            Criteria.where(param).`gt`(value.toInt())
                        }
                        "long"->{
                            Criteria.where(param).`gt`(value.toLong())
                        }
                        "float"->{
                            Criteria.where(param).`gt`(value.toFloat())
                        }
                        "double"->{
                            Criteria.where(param).`gt`(value.toDouble())
                        }
                        else -> Criteria()
                    }
                }
                "ge" ->{
                    when(valueType){
                        "string"->{
                            Criteria.where(param).`gte`(value)
                        }
                        "int"-> {
                            Criteria.where(param).`gte`(value.toInt())
                        }
                        "long"->{
                            Criteria.where(param).`gte`(value.toLong())
                        }
                        "float"->{
                            Criteria.where(param).`gte`(value.toFloat())
                        }
                        "double"->{
                            Criteria.where(param).`gte`(value.toDouble())
                        }
                        else -> Criteria()
                    }
                }
                "like" ->{
                    when(valueType){
                        "string"->{
                            Criteria.where(param).regex(".*$value.*","i")
                        }
                        else -> Criteria()
                    }
                }
                "in" ->{
                    when(valueType){
                        "string"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                Criteria().orOperator(
                                    Criteria.where(param).`in`(values),
                                    Criteria.where(param).`in`(listOf(null,"", Collections.EMPTY_LIST))
                                )
                            }else{
                                Criteria.where(param).`in`(values)
                            }
                        }
                        "int"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`in`(valuesNotNull.map { it.toInt() }),
                                    Criteria.where(param).`in`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`in`(values.map { it.toInt() })
                            }
                        }
                        "long"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`in`(valuesNotNull.map { it.toLong() }),
                                    Criteria.where(param).`in`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`in`(values.map { it.toLong() })
                            }
                        }
                        "float"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`in`(valuesNotNull.map { it.toFloat() }),
                                    Criteria.where(param).`in`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`in`(values.map { it.toFloat() })
                            }
                        }
                        "double"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`in`(valuesNotNull.map { it.toDouble() }),
                                    Criteria.where(param).`in`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`in`(values.map { it.toDouble() })
                            }
                        }
                        else -> Criteria()
                    }
                }
                "nin" ->{
                    when(valueType){
                        "string"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                Criteria().orOperator(
                                    Criteria.where(param).`nin`(values),
                                    Criteria.where(param).`nin`(listOf(null,"", Collections.EMPTY_LIST))
                                )
                            }else{
                                Criteria.where(param).`nin`(values)
                            }
                        }
                        "int"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`nin`(valuesNotNull.map { it.toInt() }),
                                    Criteria.where(param).`nin`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`nin`(values.map { it.toInt() })
                            }
                        }
                        "long"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`nin`(valuesNotNull.map { it.toLong() }),
                                    Criteria.where(param).`nin`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`nin`(values.map { it.toLong() })
                            }
                        }
                        "float"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`nin`(valuesNotNull.map { it.toFloat() }),
                                    Criteria.where(param).`nin`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`nin`(values.map { it.toFloat() })
                            }
                        }
                        "double"->{
                            val values = StringUtils.trimAllWhitespace(value).split(",")
                            if("null" in values){
                                val valuesNotNull = values.filterNot { it == "null" }
                                Criteria().orOperator(
                                    Criteria.where(param).`nin`(valuesNotNull.map { it.toDouble() }),
                                    Criteria.where(param).`nin`(null,Collections.EMPTY_LIST)
                                )
                            }else{
                                Criteria.where(param).`nin`(values.map { it.toDouble() })
                            }
                        }
                        else -> Criteria()
                    }
                }
                else -> throw CustomException.invalidExpValue(exp)
            }
        }

        fun makeWhereCriteria(param: String, exp: String, value: String):Criteria{
            return makeWhereCriteria(param,exp,value,"string")
        }
    }
}