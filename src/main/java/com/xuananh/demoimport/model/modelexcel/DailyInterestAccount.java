package com.xuananh.demoimport.model.modelexcel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyInterestAccount {
    private String truongAnLocAccount;
    private String tailocAccount;
    private String regularAccount;
    private String month6Rate12Account;
    private String dacLocAccount;
}
