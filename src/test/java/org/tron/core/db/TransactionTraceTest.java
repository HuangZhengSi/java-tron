/*
 * java-tron is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-tron is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.core.db;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.common.utils.FileUtil;
import org.tron.core.Constant;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.exception.ReceiptException;
import org.tron.protos.Contract.TriggerSmartContract;
import org.tron.protos.Protocol.ResourceReceipt;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.Protocol.Transaction.Result;
import org.tron.protos.Protocol.Transaction.raw;

@RunWith(Parameterized.class)
public class TransactionTraceTest {
  private static String dbPath = "output_TransactionTrace_test";
  private static String dbDirectory = "db_TransactionTrace_test";
  private static String indexDirectory = "index_TransactionTrace_test";
  private static AnnotationConfigApplicationContext context;
  private static Manager dbManager;

  private long cpuUsage;
  private long storageUsage;

  static {
    Args.setParam(
        new String[]{
            "--output-directory", dbPath,
            "--storage-db-directory", dbDirectory,
            "--storage-index-directory", indexDirectory,
            "-w"
        },
        Constant.TEST_CONF
    );
    context = new AnnotationConfigApplicationContext(DefaultConfig.class);
  }

  public TransactionTraceTest(long cpuUsage, long storageUsage) {
    this.cpuUsage = cpuUsage;
    this.storageUsage = storageUsage;
  }

  /**
   * resourceUsage prepare data for testing.
   * @return
   */
  @Parameters
  public static Collection resourceUsage() {
    return Arrays.asList(new Object[][] {
        {0, 0}, {6, 1000}, {7, 1000}, {10, 999}, {10, 1000}, {10, 1001}
    });
  }

  /**
   * Init data.
   */
  @BeforeClass
  public static void init() {
    dbManager = context.getBean(Manager.class);
  }

  @Test
  public void testCheckBill() {
    Transaction transaction = Transaction.newBuilder()
        .addRet(
            Result.newBuilder()
                .setReceipt(
                    ResourceReceipt.newBuilder()
                        .setCpuUsage(10)
                        .setStorageDelta(1000)
                        .build())
                .build())
        .setRawData(
            raw.newBuilder()
                .addContract(
                    Contract.newBuilder()
                        .setParameter(Any.pack(
                            TriggerSmartContract.newBuilder()
                                .setOwnerAddress(ByteString.EMPTY)
                                .build()))
                        .setType(ContractType.TriggerSmartContract)
                        .build())
                .build()
        )
        .build();

    TransactionCapsule transactionCapsule = new TransactionCapsule(transaction);

    TransactionTrace transactionTrace = new TransactionTrace(transactionCapsule, dbManager);

    transactionTrace.setBill(this.cpuUsage, this.storageUsage);

    try {
      transactionTrace.checkBill();
    } catch (ReceiptException e) {
      e.printStackTrace();
    }
  }

  /**
   * destroy clear data of testing.
   */
  @AfterClass
  public static void destroy() {
    Args.clearParam();
    FileUtil.deleteDir(new File(dbPath));
    context.destroy();
  }
}