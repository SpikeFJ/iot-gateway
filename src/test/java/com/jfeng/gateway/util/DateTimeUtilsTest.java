package com.jfeng.gateway.util;

import junit.framework.TestCase;
import lombok.Getter;
import org.junit.Test;

public class DateTimeUtilsTest extends TestCase {


    @Test
    public void test1() {
        String previous = "232301b2674d3030333136d20317070d0a201a7b8e42060677c30144010150a71f701817070d0a20192300a71f711815071601000d0300a71f72180000000000000300a71f7318a70b2400bd0bbd0ba71f7418000000000000be03a71f75185fc4030000000000a71f7618bd02da02dc020000a71f7718d502d90200000000a71f78180000000000000000a71f79180500000500000000a71f7a180000000000000000a71f7b189a00000000000000a71f7c180000002022007d00a71f7d1800000090016c0200a71f7e18df02dd02dd02da02a71f80180000005355260000a71f81180026000000000000a71f82180000000000000000a71f8318d907005f4d000000a71f8418aa000002fa000000a71f851800462000000000e9a71f86180000000081010500a71f87180000410100000000a71f8818cb0b660000000000a71f89180000000000005327a71f8a1800007d00000055eea71f8b1801006400a8de0000a71f8c184cfc000001030000a71f8d18aef60a0000000000a71f8e189901650000000000a71f9018000002c802000000a71f91182ef7000000000000a71f92188f4f7a1400000000a71f9418204e204e00000000a71f95180000000000440000a71f96182a00000000000000a71ff018000cf70000000000a71ff1180000006400000000a71ff2180000000000000000a71ff3181e00000000000000a71ff4180000000000000000a71ff5184849000000000000b01f7618ab0bbc0bb90bb90bb11f7618c00bba0bba0bbd0bb21f7618b90bc80bc00bbe0bb31f7618c40bba0bba0bbf0bb41f7618bb0bba0bba0bc10bb51f7618bb0bbb0bbe0bbe0bb61f7618bd0bbe0bbc0bbd0bb71f7618bc0bbc0bba0bbc0bb81f7618ba0bbb0bbb0bba0bb91f7618a70bc10bbd0bbd0bba1f7618bc0bbb0bbd0bbc0bbb1f7618bd0bbd0bbe0bbc0bbc1f7618c40bc60bc70bc40bbd1f7618b90bc30bc50bc40bbe1f7618c20bc00bc20bc70bbf1f7618b40bb60bc70bc10bc01f7618be0bbf0bc30bc10bc11f7618bd0bba0bbe0bab0bc21f7618b90bbd0bbc0bbe0bc31f7618ba0bbb0bbc0bb90bc41f7618bb0bba0bb90bbf0bc51f7618bb0bbc0bba0bb50bc61f7618c50bbe0bc20bba0bc71f7618ba0bbe0bba0bbe0bc81f7618bf0bbc0bbb0bbd0bc91f7618bc0bc40bcb0bba0bca1f7618c00bc10bbc0bad0bcb1f7618b80bb90bbb0bbe0bcc1f7618c00bc30bc40bbc0bcd1f7618c10bba0bc40bc60bce1f7618ad31ad31b80bb80bcf1f7618b80b4572b80bb80bd01f7618ae31ae319d0db80bd11f76183572ae31b88bdd0bd21f76186c0cb801a0f1398ed31f7618eb3eed223af41e72d41f7618cf8b06f21e3f3814d51f76186c418525c00b208e1e";
        String current = "232301b3674d30303331360a0217070d0a201a7b8e42060677c3014401012ad61f7618398eeb3e54233af4d71f76181e72cf8b06f21e3f0f10ff1817070d0a201923005601ff184c1d000000f7401f5602ff187017000000000000030eff1800000055aa0000001915ff180d020000005500f03dc5fa1800000000000000003ac2ff18000000003c005a003da6ff0c0000000000000000a7c1010c05ff2600000000001ff3211800000000000000f7a61f81180129006500000000a61f8218000002000006c902a61f8318000000001c001b30ef18ff18a00f0000e0014100efa77018010064009b9001b0efa771188e14ce27a8de0000a7ef71180000e701000000fba7ef72180300000000000200a7ef7318c80206f800010700a7ef74182800016500c40000a7ef75184948494948484100a7ef76186400000000020900a7ef771824001000efff0500a7ef78183c0005002a000b00a7ef79181300feff05003c00a7ef7a18050000000500ffffa7ef7b180000ffff00000500a7ef7c1803000000000000001743ff189001aef60a000000ef17ff1801030101a8de0001f3efc418ac0d840d90060000f3ef81180000000000000000f3ef82180000000000000000f3ef83184841010006105501f3ef84189be5ce278e140040f3ef8518a8de9ce0a8de0100f3ef8618504c4e0101031300f3ef01180000000000000000f3efc3185adf00fc29010000eff30218b0019022000000006c";
        //合并
        String merged = merge(previous, current);
        System.out.println(merged);

        System.out.println(parseTimeDetail(previous));
    }

    public String parseTimeDetail(String value){
        byte[] bytes = toBytes(value.substring(26, 38));

        StringBuilder sb = new StringBuilder();
        sb.append((bytes[0] & 0xFF) +2000);
        sb.append(fill((bytes[1] & 0xFF)));
        sb.append(fill((bytes[2] & 0xFF)));
        sb.append(fill((bytes[3] & 0xFF)));
        sb.append(fill((bytes[4] & 0xFF)));
        sb.append(fill((bytes[5] & 0xFF)));
        sb.append("000");
        return sb.toString();
    }
    public String fill(int i) {
        if (i > 0 && i < 10) {
            return '0' + Integer.toString(i);
        }
        return Integer.toString(i);
    }


    public String merge(String previous, String current) {
        Protocol previousBody = new Protocol(previous);
        Protocol currentBody = new Protocol(current);

        String mergedBody = currentBody.mergeCanData(previousBody);
        byte[] bytes = toBytes(current + mergedBody);
        byte i = bccCheck(bytes, 2, bytes.length);

        return mergedBody + currentBody.fill(i);
    }


    private byte[] toBytes(String hexString) {
        byte[] byteArray = new byte[hexString.length() / 2];

        for (int i = 0; i < byteArray.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hexString.substring(index, index + 2), 16);
            byteArray[i] = (byte) j;
        }
        return byteArray;
    }

    public byte bccCheck(byte[] value, int from, int to) {
        byte result = 0;
        for (int i = from; i < to; i++) {
            result ^= value[i];
        }
        return result;
    }
}

@Getter
class Protocol {
    public Protocol(String body) {
        parse(body);
    }

    public String header;//除数据长度以外的头数据
    public String bodyHeader;//除canNumber以为的数据头
    public int can_number;
    public String can_data;

    public void parse(String body) {
        this.header = body.substring(0, 22);
        this.bodyHeader = body.substring(26, 60);
        this.can_number = Integer.parseInt(body.substring(60, 62), 16);
        this.can_data = this.can_number > 0 ? body.substring(62, body.length() - 2) : "";
    }

    public String mergeCanData(Protocol previousBody) {
        this.can_number += previousBody.can_number;
        this.can_data += previousBody.can_data;

        StringBuilder body = new StringBuilder();
        body.append(bodyHeader);
        body.append(fill(this.can_number));
        body.append(this.can_data);
        int length = body.length() / 2;

        return this.header + fill(length % 256) + fill(length / 256) + body;
    }

    public String fill(int i) {
        if (i > 0 && i <= 16) {
            return '0' + Integer.toHexString(i);
        } else if (i < 0) {
            return Integer.toHexString(i & 0xFF);
        }
        return Integer.toHexString(i);
    }
}