/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.consensus.ibft.statemachine;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

import tech.pegasys.pantheon.consensus.ibft.ConsensusRoundIdentifier;
import tech.pegasys.pantheon.consensus.ibft.TestHelpers;
import tech.pegasys.pantheon.consensus.ibft.messagewrappers.RoundChange;
import tech.pegasys.pantheon.consensus.ibft.payload.MessageFactory;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.ethereum.core.Block;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

public class RoundChangeArtifactsTest {

  private final List<MessageFactory> messageFactories = Lists.newArrayList();

  private final long chainHeight = 5;
  private final ConsensusRoundIdentifier targetRound = new ConsensusRoundIdentifier(chainHeight, 5);

  @Before
  public void setup() {
    for (int i = 0; i < 4; i++) {
      final KeyPair keyPair = KeyPair.generate();
      final MessageFactory messageFactory = new MessageFactory(keyPair);
      messageFactories.add(messageFactory);
    }
  }

  private PreparedRoundArtifacts createPreparedRoundArtefacts(final int fromRound) {

    final ConsensusRoundIdentifier preparedRound =
        new ConsensusRoundIdentifier(chainHeight, fromRound);
    final Block block = TestHelpers.createProposalBlock(emptyList(), preparedRound);

    return new PreparedRoundArtifacts(
        messageFactories.get(0).createProposal(preparedRound, block),
        messageFactories.stream()
            .map(factory -> factory.createPrepare(preparedRound, block.getHash()))
            .collect(Collectors.toList()));
  }

  private RoundChange createRoundChange(
      final int fromRound, final boolean containsPrepareCertificate) {
    if (containsPrepareCertificate) {
      return messageFactories
          .get(0)
          .createRoundChange(targetRound, Optional.of(createPreparedRoundArtefacts(fromRound)));
    } else {
      return messageFactories.get(0).createRoundChange(targetRound, empty());
    }
  }

  @Test
  public void newestBlockIsExtractedFromListOfRoundChangeMessages() {
    final List<RoundChange> roundChanges =
        Lists.newArrayList(
            createRoundChange(1, true), createRoundChange(2, true), createRoundChange(3, false));

    RoundChangeArtifacts artifacts = RoundChangeArtifacts.create(roundChanges);

    assertThat(artifacts.getBlock()).isEqualTo(roundChanges.get(1).getProposedBlock());

    roundChanges.add(createRoundChange(4, true));
    artifacts = RoundChangeArtifacts.create(roundChanges);
    assertThat(artifacts.getBlock()).isEqualTo(roundChanges.get(3).getProposedBlock());
    assertThat(artifacts.getRoundChangeCertificate().getRoundChangePayloads())
        .containsExactly(
            roundChanges.get(0).getSignedPayload(),
            roundChanges.get(1).getSignedPayload(),
            roundChanges.get(2).getSignedPayload(),
            roundChanges.get(3).getSignedPayload());
  }

  @Test
  public void noRoundChangesPreparedThereforeReportedBlockIsEmpty() {
    final List<RoundChange> roundChanges =
        Lists.newArrayList(
            createRoundChange(1, false), createRoundChange(2, false), createRoundChange(3, false));

    final RoundChangeArtifacts artifacts = RoundChangeArtifacts.create(roundChanges);

    assertThat(artifacts.getBlock()).isEmpty();
  }
}
