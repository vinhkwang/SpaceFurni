package com.spacefurni.pricing.application;

import com.spacefurni.shared.exception.DomainException;
import com.spacefurni.shared.exception.ErrorCode;

public class PromotionNotApplicableException extends DomainException {

    public PromotionNotApplicableException(String promotionCode) {
        super(ErrorCode.PROMOTION_NOT_APPLICABLE, "Promotion code " + promotionCode + " is not valid");
    }
}
