/*
 * Copyright (C) 2019-2021 Six Clovers, Inc. - All rights reserved.
 *
 * Restricted and proprietary.
 */
package com.sixclovers.hcs;

import java.util.Collections;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;

import io.github.cdimascio.dotenv.Dotenv;

import static java.util.Objects.requireNonNull;

public class HederaTokenDemo {

    public static void main(String[] args) throws Exception {

        AccountId OPERATOR_ID = AccountId.fromString(requireNonNull(Dotenv.load().get("OPERATOR_ID")));
        PrivateKey OPERATOR_KEY = PrivateKey.fromString(requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

        Client client = Client.forTestnet();
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        final TransactionResponse txResponse = new TokenCreateTransaction()
            .setTokenName("Example 1")
            .setTokenSymbol("ex1")
            .setDecimals(0)
            .setInitialSupply(100)
            .setTreasuryAccountId(OPERATOR_ID)
            .execute(client);

        TokenId tokenId = txResponse.getReceipt(client).tokenId;

        System.out.println("Your token ID is: " + tokenId);

        TransactionResponse newAccTxResp = new AccountCreateTransaction()
            .setKey(OPERATOR_KEY)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);
        AccountId newAccountId = newAccTxResp.getReceipt(client).accountId;
        System.out.println("The new account ID is: " + newAccountId);

        final TransactionReceipt receipt = new TokenAssociateTransaction()
            .setAccountId(newAccountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        System.out.println("Your token associate transaction receipt is: " + receipt);

        TransactionReceipt tokenTransfTxReceipt = new TransferTransaction()
            .addTokenTransfer(tokenId,OPERATOR_ID, -1L)
            .addTokenTransfer(tokenId,newAccountId, 1L)
            .execute(client)
            .getReceipt(client);

        System.out.println("Your token transfer transaction receipt is: " + tokenTransfTxReceipt);
    }

}
