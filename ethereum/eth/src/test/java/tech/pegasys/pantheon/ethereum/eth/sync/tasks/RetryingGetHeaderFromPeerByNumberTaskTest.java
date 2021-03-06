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
package tech.pegasys.pantheon.ethereum.eth.sync.tasks;

import static java.util.Collections.singletonList;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.eth.manager.EthTask;
import tech.pegasys.pantheon.ethereum.eth.manager.ethtaskutils.RetryingMessageTaskTest;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class RetryingGetHeaderFromPeerByNumberTaskTest
    extends RetryingMessageTaskTest<List<BlockHeader>> {

  private static final long PIVOT_BLOCK_NUMBER = 10;

  @Override
  protected List<BlockHeader> generateDataToBeRequested() {
    return singletonList(blockchain.getBlockHeader(PIVOT_BLOCK_NUMBER).get());
  }

  @Override
  protected EthTask<List<BlockHeader>> createTask(final List<BlockHeader> requestedData) {
    return RetryingGetHeaderFromPeerByNumberTask.forSingleNumber(
        protocolSchedule, ethContext, ethTasksTimer, PIVOT_BLOCK_NUMBER, maxRetries);
  }

  @Test
  @Override
  @Ignore("It's not possible to return a partial result as we only ever request one header.")
  public void failsWhenPeerReturnsPartialResultThenStops() {}
}
