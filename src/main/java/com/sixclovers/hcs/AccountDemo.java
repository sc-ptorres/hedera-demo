/*
 * Copyright (C) 2019-2021 Six Clovers, Inc. - All rights reserved.
 *
 * Restricted and proprietary.
 */
package com.sixclovers.hcs;

import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountBalance;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionResponse;

import io.github.cdimascio.dotenv.Dotenv;

public class AccountDemo {

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("OPERATOR_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("OPERATOR_KEY"));

        Client client = Client.forTestnet();
        client.setOperator(myAccountId, myPrivateKey);

        PrivateKey newAccountPrivateKey = PrivateKey.generate();
        PublicKey newAccountPublicKey = newAccountPrivateKey.getPublicKey();

        TransactionResponse newAccount = new AccountCreateTransaction()
            .setKey(newAccountPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        AccountId newAccountId = newAccount.getReceipt(client).accountId;

        System.out.println("The new account ID is: " + newAccountId);

        AccountBalance accountBalance = new AccountBalanceQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("The new account balance is: " + accountBalance.hbars);
    }

}
