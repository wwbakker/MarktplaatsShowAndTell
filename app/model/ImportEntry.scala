package model

import java.time.LocalDate

case class ImportEntry(name : String,
                       address: String,
                       postalCode : String,
                       phoneNumber : String,
                       creditLimitInCents : Int,
                       birthday : LocalDate)
