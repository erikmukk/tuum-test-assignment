package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionDao {

    List<TransactionEntity> getByAccountId(@Param("accountId") String accountId);

    int insert(@Param("transaction") TransactionEntity transaction);
}
