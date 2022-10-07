package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BalanceDao {
    List<BalanceEntity> getBalancesByAccountId(@Param("accountId") String accountId);

    BalanceEntity getBalanceByAccountIdForUpdate(@Param("accountId") String accountId, @Param("currency") String currency);

    List<Currency> getAccountCurrencies(@Param("accountId") String accountId);

    int insert(@Param("balance") BalanceEntity balance);

    int updateByPrimaryKey(BalanceEntity row);
}
