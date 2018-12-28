package stest.tron.wallet.multiSign.updateAccountPermission;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Key;
import org.tron.protos.Protocol.Permission;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;
import stest.tron.wallet.common.client.utils.Sha256Hash;

@Slf4j
public class MultiSignUpdateAccountPermission029 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethed.getFinalAddress(testKey002);

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");

  private static final long now = System.currentTimeMillis();
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private static ByteString assetAccountId = null;
  private static final long TotalSupply = 1000L;
  private byte[] transferTokenContractAddress = null;

  private String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  private String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");


  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  @BeforeClass(enabled = true)
  public void beforeClass() {

    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

  }

//  @AfterClass(enabled = true)
//  public void afterClass() {
//    Assert.assertTrue(PublicMethed.unFreezeBalance(fromAddress, testKey002, 1,
//        dev001Address, blockingStubFull));
//    Assert.assertTrue(PublicMethed.unFreezeBalance(fromAddress, testKey002, 0,
//        dev001Address, blockingStubFull));
//  }

  private List<String> getStrings(byte[] data){
    int index = 0;
    List<String> ret = new ArrayList<>();
    while(index < data.length){
      ret.add(byte2HexStr(data, index, 32));
      index += 32;
    }
    return ret;
  }

  public static String byte2HexStr(byte[] b, int offset, int length) {
    String stmp="";
    StringBuilder sb = new StringBuilder("");
    for (int n= offset; n<offset + length && n < b.length; n++) {
      stmp = Integer.toHexString(b[n] & 0xFF);
      sb.append((stmp.length()==1)? "0"+stmp : stmp);
    }
    return sb.toString().toUpperCase().trim();
  }

  @Test
  public void updatePermissionTest() {
//    ECKey ecKey1 = new ECKey(Utils.getRandom());
//    byte[] dev001Address = ecKey1.getAddress();
//    String dev001Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    String dev001Key  = "99454aca732c9335c32fedb56ee552f4e410d61a1390763f458187e162a4840b";
    byte[] dev001Address = new WalletClient(dev001Key).getAddress();
    PublicMethed.printAddress(dev001Key);
//    PublicMethed.sendcoin(dev001Address, 100_000000, fromAddress, testKey002, blockingStubFull);

//    String permission = "[{\"keys\":[{\"address\":\"THph9K2M2nLvkianrMGswRhz5hjSA9fuH7\",\"weight\":2},{\"address\":\"TWNxQr5QpuH6FMTDesf4PJEshPumS8fhZ7\",\"weight\":2147483647},{\"address\":\"TR3FAbhiSeP7kSh39RjGYpwCqfMDHPMhX4\",\"weight\":2147483647}],\"name\":\"owner\",\"threshold\":1,\"parent\":\"owner\"},{\"parent\":\"owner\",\"keys\":[{\"address\":\"TWNxQr5QpuH6FMTDesf4PJEshPumS8fhZ7\",\"weight\":2},{\"address\":\"THph9K2M2nLvkianrMGswRhz5hjSA9fuH7\",\"weight\":3}],\"name\":\"active\",\"threshold\":5}]";
    String permission = "[{\"keys\":[{\"address\":\"THph9K2M2nLvkianrMGswRhz5hjSA9fuH7\",\"weight\":2},{\"address\":\"TWNxQr5QpuH6FMTDesf4PJEshPumS8fhZ7\",\"weight\":2147483647},{\"address\":\"TR3FAbhiSeP7kSh39RjGYpwCqfMDHPMhX4\",\"weight\":2147483647}],\"name\":\"owner\",\"threshold\":1,\"parent\":\"owner\"},{\"parent\":\"owner\",\"keys\":[{\"address\":\"TWNxQr5QpuH6FMTDesf4PJEshPumS8fhZ7\",\"weight\":2},{\"address\":\"THph9K2M2nLvkianrMGswRhz5hjSA9fuH7\",\"weight\":3}],\"name\":\"active\",\"threshold\":5}]";
    boolean ret = PublicMethed.accountPermissionUpdate(permission, dev001Address, dev001Key, blockingStubFull);

    Account test001AddressAccount = PublicMethed.queryAccount(dev001Address, blockingStubFull);
    List<Permission> permissionsList = test001AddressAccount.getPermissionsList();
    printPermissionList(permissionsList);

    Assert.assertTrue(ret);
  }

  public static void printPermissionList(List<Permission> permissionList) {
    String result = "\n";
    result += "[";
    result += "\n";
    int i = 0;
    for (Permission permission : permissionList) {
      result += "permission " + i + " :::";
      result += "\n";
      result += "{";
      result += "\n";
      result += printPermission(permission);
      result += "\n";
      result += "}";
      result += "\n";
      i++;
    }
    result += "]";
    System.out.println(result);
  }

  public static String printPermission(Permission permission) {
    StringBuffer result = new StringBuffer();
    result.append("name: ");
    result.append(permission.getName());
    result.append("\n");
    result.append("threshold: ");
    result.append(permission.getThreshold());
    result.append("\n");
    if (permission.getKeysCount() > 0) {
      result.append("keys:");
      result.append("\n");
      result.append("[");
      result.append("\n");
      for (Key key : permission.getKeysList()) {
        result.append(printKey(key));
      }
      result.append("]");
      result.append("\n");
    }
    return result.toString();
  }

  public static String printKey(Key key) {
    StringBuffer result = new StringBuffer();
    result.append("address: ");
    result.append(encode58Check(key.getAddress().toByteArray()));
    result.append("\n");
    result.append("weight: ");
    result.append(key.getWeight());
    result.append("\n");
    return result.toString();
  }

  public static String encode58Check(byte[] input) {
    byte[] hash0 = Sha256Hash.hash(input);
    byte[] hash1 = Sha256Hash.hash(hash0);
    byte[] inputCheck = new byte[input.length + 4];
    System.arraycopy(input, 0, inputCheck, 0, input.length);
    System.arraycopy(hash1, 0, inputCheck, input.length, 4);
    return Base58.encode(inputCheck);
  }


  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


