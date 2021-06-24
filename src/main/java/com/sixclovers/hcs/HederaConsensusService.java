/*
 * Copyright (C) 2019-2021 Six Clovers, Inc. - All rights reserved.
 *
 * Restricted and proprietary.
 */
package com.sixclovers.hcs;

import java.nio.charset.StandardCharsets;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicId;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.TransactionResponse;

import io.github.cdimascio.dotenv.Dotenv;

import static java.util.Objects.requireNonNull;

public class HederaConsensusService {

    public static void main(String[] args) throws Exception {

        AccountId OPERATOR_ID = AccountId.fromString(requireNonNull(Dotenv.load().get("OPERATOR_ID")));
        PrivateKey OPERATOR_KEY = PrivateKey.fromString(requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

        // Build Hedera testnet and mirror node client
        Client client = Client.forTestnet();

        // Set the operator account ID and operator private key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        //Create a new topic
        final TransactionResponse txResponse = new TopicCreateTransaction()
            .execute(client);

        //Grab the newly generated topic ID
        TopicId topicId = txResponse.getReceipt(client).topicId;

        System.out.println("Your topic ID is: " + topicId);
        Thread.sleep(5000);

        new com.hedera.hashgraph.sdk.TopicMessageQuery()
            .setTopicId(topicId)
            .subscribe(client, resp -> {
                String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);

                System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
            });

        for (int i = 0; i < 5; i++) {
            //Submit a message to a topic
            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("This is hello, HCS! number " + (i + 1))
                .execute(client)
                .getReceipt(client);

            Thread.sleep(5000);
        }

    }

}
