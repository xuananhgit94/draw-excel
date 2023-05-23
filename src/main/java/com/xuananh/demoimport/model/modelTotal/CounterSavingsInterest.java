package com.xuananh.demoimport.model.modelTotal;

import com.xuananh.demoimport.model.modelexcel.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CounterSavingsInterest {
    private String depositTerm;
    private DailyInterestAccount dailyInterestAccount;
    private InitialInterestSavings initialInterestSavings;
    private RegularInterestSavings regularInterestSavings;
    private ContributoryAccount contributoryAccount;
    private BaoAnLocDeposit baoAnLocDeposit;
    private Map<String, List<String>> flexibleInterestRate;
}
