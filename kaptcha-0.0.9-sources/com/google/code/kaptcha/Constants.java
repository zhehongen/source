package com.google.code.kaptcha;

public class Constants
{
	public final static String KAPTCHA_SESSION_KEY = "KAPTCHA_SESSION_KEY";

	public final static String KAPTCHA_SESSION_DATE = "KAPTCHA_SESSION_DATE";

	public final static String KAPTCHA_SESSION_CONFIG_KEY = "kaptcha.session.key";

	public final static String KAPTCHA_SESSION_CONFIG_DATE = "kaptcha.session.date";

	public final static String KAPTCHA_BORDER = "kaptcha.border";//是否包含边框true

	public final static String KAPTCHA_BORDER_COLOR = "kaptcha.border.color";//边框颜色black

	public final static String KAPTCHA_BORDER_THICKNESS = "kaptcha.border.thickness";//边框宽度 1

	public final static String KAPTCHA_NOISE_COLOR = "kaptcha.noise.color";//噪声颜色 black

	public final static String KAPTCHA_NOISE_IMPL = "kaptcha.noise.impl";//默认生成噪声的类DefaultNoise

	public final static String KAPTCHA_OBSCURIFICATOR_IMPL = "kaptcha.obscurificator.impl";//遮蔽器工具WaterRipple

	public final static String KAPTCHA_PRODUCER_IMPL = "kaptcha.producer.impl";//聚合类 DefaultKaptcha

	public final static String KAPTCHA_TEXTPRODUCER_IMPL = "kaptcha.textproducer.impl";//生成随机数DefaultTextCreator

	public final static String KAPTCHA_TEXTPRODUCER_CHAR_STRING = "kaptcha.textproducer.char.string";//候选字符abcde2345678gfynmnpwx

	public final static String KAPTCHA_TEXTPRODUCER_CHAR_LENGTH = "kaptcha.textproducer.char.length";//几个字符5

	public final static String KAPTCHA_TEXTPRODUCER_FONT_NAMES = "kaptcha.textproducer.font.names";//字体名称，逗号分隔Arial,Courier

	public final static String KAPTCHA_TEXTPRODUCER_FONT_COLOR = "kaptcha.textproducer.font.color";//字体颜色 BLACK

	public final static String KAPTCHA_TEXTPRODUCER_FONT_SIZE = "kaptcha.textproducer.font.size";//字体大小 40

	public final static String KAPTCHA_TEXTPRODUCER_CHAR_SPACE = "kaptcha.textproducer.char.space";//字体之间的间隔 2

	public final static String KAPTCHA_WORDRENDERER_IMPL = "kaptcha.word.impl";//对字符进行渲染的实现类DefaultWordRenderer

	public final static String KAPTCHA_BACKGROUND_IMPL = "kaptcha.background.impl";//背景渲染器 DefaultBackground

	public static final String KAPTCHA_BACKGROUND_CLR_FROM = "kaptcha.background.clear.from";//背景开始颜色LIGHT_GRAY

	public static final String KAPTCHA_BACKGROUND_CLR_TO = "kaptcha.background.clear.to";//背景结束颜色WHITE

	public static final String KAPTCHA_IMAGE_WIDTH = "kaptcha.image.width";

	public static final String KAPTCHA_IMAGE_HEIGHT = "kaptcha.image.height";
}
