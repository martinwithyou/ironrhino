package org.ironrhino.common.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.HttpClientUtils;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.util.XmlUtils;

import com.fasterxml.jackson.databind.JsonNode;

public class LocationParser {

	static class Holder {
		static LocationParser instance = new LocationParser();
	}

	private static final int ADDRESS_RECORD_LENGTH = 7;

	private static final byte AREA_FOLLOWED = 0x01;

	private static final byte NO_AREA = 0x2;

	private RandomAccessFile file;

	private long start, end;

	private byte[] buf;

	private byte[] b4;

	private byte[] b3;

	private boolean available;

	private static List<String> specialList1 = Arrays.asList(new String[] {
			"内蒙古", "新疆", "西藏", "广西", "宁夏" });

	private static List<String> specialList2 = Arrays.asList(new String[] {
			"香港", "澳门" });

	private static List<String> specialList3 = Arrays.asList(new String[] {
			"北京", "上海", "天津", "重庆" });

	private LocationParser() {
		buf = new byte[100];
		b4 = new byte[4];
		b3 = new byte[3];
		try {
			File f = new File(AppInfo.getAppHome() + "/data/wry.dat");
			if (f.exists()) {
				file = new RandomAccessFile(f, "r");
			} else {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				if (cl == null)
					cl = LocationParser.class.getClassLoader();
				URL url = cl.getResource("resources/data/wry.dat");
				if (url != null)
					file = new RandomAccessFile(url.getFile(), "r");
			}
			start = readLong4(0);
			end = readLong4(4);
			if (start == -1 || end == -1) {
				file.close();
				file = null;
			}
			available = true;
		} catch (Exception e) {
			file = null;
			e.printStackTrace();
		}
	}

	public static Location parseLocal(String host) {
		LocationParser instance = Holder.instance;
		if (instance == null || !instance.available)
			return null;
		try {
			String[] loc = instance.parseLocation(InetAddress.getByName(host)
					.getAddress());
			String string = (loc[0] != null ? loc[0] : "")
					+ (loc[1] != null ? loc[1] : "");
			Location location = new Location(string);
			for (String s : specialList1)
				if (string.startsWith(s)) {
					location.setFirstArea(s);
					if (string.indexOf("自治区") > 0)
						location.setSecondArea(string.substring(
								string.indexOf("自治区") + 3, string.indexOf("市")));
					else
						location.setSecondArea(string.substring(s.length(),
								string.indexOf("市")));
					if (string.indexOf("区") > string.indexOf("市"))
						location.setThirdArea(string.substring(
								string.indexOf("市") + 1, string.indexOf("区")));
					return location;
				}

			for (String s : specialList2)
				if (string.startsWith(s)) {
					location.setFirstArea(s);
					return location;
				}

			if (string.indexOf("省") > 0) {
				location.setFirstArea(string.substring(0, string.indexOf("省")));
				if (string.indexOf("市") > string.indexOf("省") + 1)
					location.setSecondArea(string.substring(
							string.indexOf("省") + 1, string.indexOf("市")));
				if (string.indexOf("区") > string.indexOf("市") + 1)
					location.setThirdArea(string.substring(
							string.indexOf("市") + 1, string.indexOf("区")));
				return location;
			} else {
				if (string.indexOf("市") > 0)
					location.setFirstArea(string.substring(0,
							string.indexOf("市")));
				if (string.indexOf("区") > string.indexOf("市") + 1)
					location.setSecondArea(string.substring(
							string.indexOf("市") + 1, string.indexOf("区")));
				if (string.indexOf("县") > string.indexOf("市") + 1)
					location.setSecondArea(string.substring(
							string.indexOf("县") + 1, string.indexOf("区")));
				return location;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private String[] parseLocation(byte[] address) throws Exception {
		String[] loc = new String[2];
		long offset = locateAddress(address);
		file.seek(offset + 4);
		byte b = file.readByte();
		if (b == AREA_FOLLOWED) {
			long countryOffset = readLong3();
			file.seek(countryOffset);
			b = file.readByte();
			if (b == NO_AREA) {
				loc[0] = readString(readLong3());
				file.seek(countryOffset + 4);
			} else
				loc[0] = readString(countryOffset);
			loc[1] = readArea(file.getFilePointer());
		} else if (b == NO_AREA) {
			loc[0] = readString(readLong3());
			loc[1] = readArea(offset + 8);
		} else {
			loc[0] = readString(file.getFilePointer() - 1);
			loc[1] = readArea(file.getFilePointer());
		}
		return loc;
	}

	private long readLong4(long offset) {
		long ret = 0;
		try {
			file.seek(offset);
			ret |= (file.readByte() & 0xFF);
			ret |= ((file.readByte() << 8) & 0xFF00);
			ret |= ((file.readByte() << 16) & 0xFF0000);
			ret |= ((file.readByte() << 24) & 0xFF000000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	private long readLong3(long offset) {
		long ret = 0;
		try {
			file.seek(offset);
			file.readFully(b3);
			ret |= (b3[0] & 0xFF);
			ret |= ((b3[1] << 8) & 0xFF00);
			ret |= ((b3[2] << 16) & 0xFF0000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	private long readLong3() {
		long ret = 0;
		try {
			file.readFully(b3);
			ret |= (b3[0] & 0xFF);
			ret |= ((b3[1] << 8) & 0xFF00);
			ret |= ((b3[2] << 16) & 0xFF0000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	private void readAddress(long offset, byte[] address) {
		try {
			file.seek(offset);
			file.readFully(address);
			byte temp = address[0];
			address[0] = address[3];
			address[3] = temp;
			temp = address[1];
			address[1] = address[2];
			address[2] = temp;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int compareAddress(byte[] address, byte[] beginAddress) {
		for (int i = 0; i < 4; i++) {
			int r = compareByte(address[i], beginAddress[i]);
			if (r != 0)
				return r;
		}
		return 0;
	}

	private int compareByte(byte b1, byte b2) {
		if ((b1 & 0xFF) > (b2 & 0xFF))
			return 1;
		else if ((b1 ^ b2) == 0)
			return 0;
		else
			return -1;
	}

	private long locateAddress(byte[] address) {
		long m = 0;
		int r;
		readAddress(start, b4);
		r = compareAddress(address, b4);
		if (r == 0)
			return start;
		else if (r < 0)
			return -1;
		for (long i = start, j = end; i < j;) {
			m = getMiddleOffset(i, j);
			readAddress(m, b4);
			r = compareAddress(address, b4);
			if (r > 0)
				i = m;
			else if (r < 0) {
				if (m == j) {
					j -= ADDRESS_RECORD_LENGTH;
					m = j;
				} else
					j = m;
			} else
				return readLong3(m + 4);
		}
		m = readLong3(m + 4);
		readAddress(m, b4);
		r = compareAddress(address, b4);
		if (r <= 0)
			return m;
		else
			return -1;
	}

	private long getMiddleOffset(long begin, long end) {
		long records = (end - begin) / ADDRESS_RECORD_LENGTH;
		records >>= 1;
		if (records == 0)
			records = 1;
		return begin + records * ADDRESS_RECORD_LENGTH;
	}

	private String readArea(long offset) throws Exception {
		file.seek(offset);
		byte b = file.readByte();
		if (b == 0x01 || b == 0x02) {
			long areaOffset = readLong3(offset + 1);
			if (areaOffset == 0)
				return null;
			else
				return readString(areaOffset);
		} else
			return readString(offset);
	}

	private String readString(long offset) throws Exception {
		file.seek(offset);
		int i;
		for (i = 0, buf[i] = file.readByte(); buf[i] != 0; buf[++i] = file
				.readByte())
			;
		if (i != 0)
			return new String(buf, 0, i, "GBK");
		return null;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		if (file != null)
			file.close();
	}

	public static Location parse(String value) {
		if (StringUtils.isBlank(value))
			return null;
		Location loc = null;
		if (value.split("\\.").length == 4) {
			try {
				loc = parseLocal(value);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			if (loc == null || loc.getFirstArea() == null) {
				try {
					String json = HttpClientUtils
							.getResponseText("http://ip.taobao.com/service/getIpInfo.php?ip="
									+ value);
					JsonNode node = JsonUtils.fromJson(json, JsonNode.class);
					if (node != null && node.get("code").asInt() == 0) {
						node = node.get("data");
						if (StringUtils.isNotBlank(node.get("region").asText())) {
							loc = new Location();
							loc.setFirstArea(node.get("region").asText());
							loc.setSecondArea(node.get("city").asText());
							loc.setThirdArea(node.get("county").asText());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (StringUtils.isNumeric(value)) {
			try {
				String xml = HttpClientUtils
						.getResponseText("http://www.youdao.com/smartresult-xml/search.s?type=mobile&q="
								+ value);
				String location = XmlUtils.eval(
						"/smartresult/product/location", xml);
				if (StringUtils.isNotBlank(location)) {
					loc = new Location();
					loc.setLocation(location.trim());
					String[] arr = loc.getLocation().split("\\s+");
					if (arr.length > 1)
						loc.setSecondArea(arr[1]);
					loc.setFirstArea(arr[0]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (value.length() >= 2) {
			String s = value.substring(0, 2);
			if (specialList2.contains(s) || specialList3.contains(s)) {
				loc = new Location();
				loc.setLocation(value);
				loc.setFirstArea(s);
				loc.setSecondArea(s);
				int index = value.indexOf("市");
				value = index > 0 ? value.substring(index + 1) : value
						.substring(s.length());
				if (StringUtils.isNotBlank(value)) {
					if ((index = value.indexOf("区")) > 0) {
						loc.setThirdArea(LocationUtils.shortenName(value
								.substring(0, index + 1)));
						value = value.substring(index + 1);
					} else if ((index = value.indexOf("县")) > 0) {
						loc.setThirdArea(LocationUtils.shortenName(value
								.substring(0, index + 1)));
						value = value.substring(index + 1);
					}
				}
				return loc;
			}
			boolean isSpecialList1 = false;
			for (String str : specialList1)
				if (value.startsWith(str)) {
					isSpecialList1 = true;
					loc = new Location();
					loc.setLocation(value);
					loc.setFirstArea(str);
					int index = value.indexOf("自治区");
					value = index > 0 ? value.substring(index + 3) : value
							.substring(str.length());
					if (StringUtils.isNotBlank(value)) {
						boolean hasSecond = true;
						if ((index = value.indexOf("市")) > 0) {
							loc.setSecondArea(value.substring(0, index + 1));
							value = value.substring(index + 1);
						} else if ((index = value.indexOf("地区")) > 0) {
							loc.setSecondArea(value.substring(0, index + 2));
							value = value.substring(index + 2);
						} else if ((index = value.indexOf("州")) > 0) {
							loc.setSecondArea(value.substring(0, index + 1));
							value = value.substring(index + 1);
						} else {
							hasSecond = false;
						}
						if (hasSecond) {
							if ((index = value.indexOf("区")) > 0) {
								loc.setThirdArea(value.substring(0, index + 1));
								value = value.substring(index + 1);
							} else if ((index = value.indexOf("县")) > 0) {
								loc.setThirdArea(value.substring(0, index + 1));
								value = value.substring(index + 1);
							}
						}
					}
				}
			if (!isSpecialList1) {
				int index = value.indexOf("省");
				if (index == 2 || index == 3) {
					loc = new Location();
					loc.setLocation(value);
					loc.setFirstArea(value.substring(0, index + 1));
					value = value.substring(index + 1);
					if (StringUtils.isNotBlank(value)) {
						boolean hasSecond = true;
						if ((index = value.indexOf("市")) > 0) {
							loc.setSecondArea(value.substring(0, index + 1));
							value = value.substring(index + 1);
						} else if ((index = value.indexOf("地区")) > 0) {
							loc.setSecondArea(value.substring(0, index + 2));
							value = value.substring(index + 2);
						} else if ((index = value.indexOf("州")) > 0) {
							loc.setSecondArea(value.substring(0, index + 1));
							value = value.substring(index + 1);
						} else {
							hasSecond = false;
						}
						if (hasSecond) {
							if ((index = value.indexOf("区")) > 0) {
								loc.setThirdArea(value.substring(0, index + 1));
								value = value.substring(index + 1);
							} else if ((index = value.indexOf("县")) > 0) {
								loc.setThirdArea(value.substring(0, index + 1));
								value = value.substring(index + 1);
							}
						}
					}
				}
			}
		}
		if (loc != null) {
			if (loc.getThirdArea() != null)
				loc.setThirdArea(LocationUtils.shortenName(loc.getThirdArea()));
			if (loc.getSecondArea() != null)
				loc.setSecondArea(LocationUtils.shortenName(loc.getSecondArea()));
			if (loc.getFirstArea() != null) {
				loc.setFirstArea(LocationUtils.shortenName(loc.getFirstArea()));
				if (specialList2.contains(loc.getFirstArea())
						|| specialList3.contains(loc.getFirstArea()))
					loc.setSecondArea(loc.getFirstArea());
			}

		}
		return loc;
	}

}
