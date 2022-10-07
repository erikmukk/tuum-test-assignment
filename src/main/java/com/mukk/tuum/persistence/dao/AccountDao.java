package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.persistence.entity.AccountBalance;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountDao {
    AccountBalance getAccountWithBalances(@Param("accountId") String accountId);

    int insert(@Param("account") AccountEntity account);

    AccountEntity selectByPrimaryKey(String accountId);

    List<AccountEntity> getAll();

}
