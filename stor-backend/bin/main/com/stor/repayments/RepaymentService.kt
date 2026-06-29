package com.stor.repayments

import com.stor.common.ApiException
import com.stor.repayments.models.*
import java.util.UUID

class RepaymentService(private val repo: RepaymentRepository = RepaymentRepository()) {

    fun list(userId: String, loanId: String) =
        repo.findByLoan(UUID.fromString(loanId), UUID.fromString(userId))

    fun create(userId: String, loanId: String, req: CreateRepaymentRequest): RepaymentDto {
        if (req.amountPaid <= 0) throw ApiException.badRequest("Amount must be positive")
        return repo.create(UUID.fromString(loanId), UUID.fromString(userId), req)
    }
}
