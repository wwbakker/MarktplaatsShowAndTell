package model

import java.time.LocalDate

case class CreditLimit(name : String,
                       address: String,
                       postalCode : String,
                       phoneNumber : String,
                       creditLimitInCents : Int,
                       birthday : LocalDate)
