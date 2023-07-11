package com.jfeng.gateway.util;

public class PerformanceUtils {

	private PerformanceUtils() {

	}

	private void integerBinary(int num) {
		int value = 0x3F;
		long start = System.currentTimeMillis();

		for (int i = 0; i < num; i++) {
			Integer.toBinaryString(0x3F);
		}
		long total = System.currentTimeMillis() - start;
		System.out.println("integerBinary耗时：" + total);
	}

	private void integerBitShift(int num) {
		int value = 0x3F;
		long start = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			int[] chars = new int[8];
			chars[0] = value & 0x01;
			chars[1] = (value >> 1) & 0x01;
			chars[2] = (value >> 2) & 0x01;
			chars[3] = (value >> 3) & 0x01;
			chars[4] = (value >> 4) & 0x01;
			chars[5] = (value >> 5) & 0x01;
			chars[6] = (value >> 6) & 0x01;
			chars[7] = (value >> 7) & 0x01;
		}
		long total = System.currentTimeMillis() - start;
		System.out.println("integerBitShift耗时：" + total);
	}

	private static int[] AND = {0x1, 0x3, 0x7, 0xF, 0x1F, 0x3F, 0x7F, 0xFF};

	/**
	 * 基于移位运算的解析
	 *
	 * @param value
	 * @param startIndex
	 * @param length
	 * @return
	 */
	public static long calcValueFast(byte[] value, int startIndex, int length) {
		if (startIndex + length > value.length * 8) {
			return 0;
		}

		int startByte = startIndex / 8;
		int startBit = startIndex - startByte * 8;

		int endIndex = startIndex + length;
		int endByte = endIndex % 8 == 0 ? (endIndex / 8 - 1) : endIndex / 8;
		int endBit = (startIndex + length - 1) % 8;

		//1.单字节
		if (startByte == endByte) {
			byte tmp = value[startByte];
			return (tmp >> startBit) & AND[endBit - startBit];
		}
		//2.双字节
		if (startByte + 1 == endByte) {
			byte byte1 = value[startByte];
			byte byte2 = value[endByte];
			return (byte1 >> startBit) | ((byte2 & AND[endBit]) << (8 - startBit));
		}
		//多字节
		long result = 0;
		int accum = 0;
		for (int i = startByte; i < value.length; i++) {
			if (i == startByte) {
				result = value[i] >> startBit;
			} else if (i == endByte) {
				result |= ((value[i] & AND[endBit]) << (8 - startBit + 8 * accum));
			} else {
				result |= (value[i] << (8 - startBit + 8 * accum));
			}

			accum++;
		}
		return result;
	}

	/**
	 * 基于位数组计算
	 *
	 * @param value
	 * @param startIndex
	 * @param length
	 * @return
	 */
	public static int calcValue2(byte[] value, int startIndex, int length) {
		byte[] bitStore = new byte[value.length * 8];
		for (int i = 0; i < value.length; i++) {
			int start = i * 8;
			int tmp = value[i];
			bitStore[start] = (byte) (tmp & 0x01);
			bitStore[start + 1] = (byte) ((tmp >> 1) & 0x01);
			bitStore[start + 2] = (byte) ((tmp >> 2) & 0x01);
			bitStore[start + 3] = (byte) ((tmp >> 3) & 0x01);
			bitStore[start + 4] = (byte) (((tmp & 0xF0) >> 4) & 0x01);
			bitStore[start + 5] = (byte) (((tmp & 0xF0) >> 5) & 0x01);
			bitStore[start + 6] = (byte) (((tmp & 0xF0) >> 6) & 0x01);
			bitStore[start + 7] = (byte) (((tmp & 0xF0) >> 7) & 0x01);
		}

		int result = 0;
		int bitShift = 0;
		for (int i = startIndex; i < startIndex + length; i++) {
			result |= (bitStore[i] << bitShift);
			bitShift++;
		}

		return result;
	}


	public static void main(String[] args) {
		PerformanceUtils performanceUtils = new PerformanceUtils();
		performanceUtils.integerBinary(10000000);
		performanceUtils.integerBitShift(10000000);

		byte[] testData = new byte[8];
		testData[0] = 0x3F;
		testData[1] = 0x55;
		testData[2] = 0x4a;
		testData[3] = 0x68;
		testData[4] = 0x7c;
		testData[5] = 0x6c;
		testData[6] = 0x1a;
		testData[7] = 0xb;

		byte a = (byte) 0x85;
		int b = a;
		System.out.println(b);
		System.out.println((b & 0xF0) >> 4);
		System.out.println((0x85 >> 4));

		System.out.println(calcValueFast(testData, 0, 8));
		System.out.println(calcValue2(testData, 0, 8));
		System.out.println(calcValueFast(testData, 9, 4));
		System.out.println(calcValue2(testData, 9, 4));
		System.out.println(calcValueFast(testData, 13, 4));
		System.out.println(calcValue2(testData, 13, 4));
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			performanceUtils.calcValueFast(testData, 17, 12);
		}
		System.out.println(System.currentTimeMillis() - start);

		start = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
			performanceUtils.calcValue2(testData, 17, 12);
		}
		System.out.println(System.currentTimeMillis() - start);
		System.out.println(performanceUtils.calcValueFast(testData, 29, 35));
		System.out.println(performanceUtils.calcValue2(testData, 29, 35));
	}
}
