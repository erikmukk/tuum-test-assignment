package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.model.response.AccountResponse;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountDao {
    AccountResponse getAccountWithBalances(@Param("accountId") String accountId);

    int insert(@Param("account") AccountEntity account);

    AccountEntity selectByPrimaryKey(String accountId);

    List<AccountEntity> getAll();

}
