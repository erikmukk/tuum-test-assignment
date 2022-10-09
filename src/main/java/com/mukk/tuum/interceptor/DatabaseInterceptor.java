package com.mukk.tuum.interceptor;

import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.service.RabbitSender;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.Aspects;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

@Aspect
@Slf4j
public class DatabaseInterceptor {

    @Autowired
    private RabbitSender rabbitSender;

    @Pointcut("if()")
    public static boolean isActive() {
        return Aspects.aspectOf(DatabaseInterceptor.class).rabbitSender != null;
    }

    @AfterReturning(pointcut = "isActive() && execution(* com.mukk.tuum.service.AccountService.insertAccount(..))", returning = "insertResult")
    public void sendMessageAfterAccountInsert(JoinPoint jp, Integer insertResult) {
        if (insertResult == 1) {
            if (jp.getArgs().length == 1) {
                rabbitSender.send(RabbitDatabaseAction.INSERT, RabbitDatabaseTable.ACCOUNT, jp.getArgs()[0]);
            } else {
                log.error("Not publishing RabbitMQ message on account insert, account entity missing");
            }
        }
    }

    @AfterReturning(pointcut = "isActive() && execution(* com.mukk.tuum.service.BalanceService.insertBalance(..))", returning = "insertResult")
    public void sendMessageAfterBalanceInsert(JoinPoint jp, Integer insertResult) {
        if (insertResult == 1) {
            if (jp.getArgs().length == 1) {
                rabbitSender.send(RabbitDatabaseAction.INSERT, RabbitDatabaseTable.BALANCE, jp.getArgs()[0]);
            } else {
                log.error("Not publishing RabbitMQ message on balance insert, balance entity missing");
            }
        }
    }

    @AfterReturning(pointcut = "isActive() && execution(* com.mukk.tuum.service.BalanceService.updateBalance(..))", returning = "updateResult")
    public void sendMessageAfterBalanceUpdate(JoinPoint jp, Integer updateResult) {
        if (updateResult == 1) {
            if (jp.getArgs().length == 1) {
                rabbitSender.send(RabbitDatabaseAction.UPDATE, RabbitDatabaseTable.BALANCE, jp.getArgs()[0]);
            } else {
                log.error("Not publishing RabbitMQ message on balance update, balance entity missing");
            }
        }
    }

    @AfterReturning(pointcut = "isActive() && execution(* com.mukk.tuum.service.TransactionService.insertTransaction(..))", returning = "insertResult")
    public void sendMessageAfterTransactionInsert(JoinPoint jp, Integer insertResult) {
        if (insertResult == 1) {
            if (jp.getArgs().length == 1) {
                rabbitSender.send(RabbitDatabaseAction.INSERT, RabbitDatabaseTable.TRANSACTION, jp.getArgs()[0]);
            } else {
                log.error("Not publishing RabbitMQ message on transaction insert, balance entity missing");
            }
        }
    }
}
