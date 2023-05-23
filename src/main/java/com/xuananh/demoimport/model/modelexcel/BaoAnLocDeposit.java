package com.xuananh.demoimport.model.modelexcel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaoAnLocDeposit {
    private String endOfTerm;
    private String monthly;
    private String quarterly;
}
