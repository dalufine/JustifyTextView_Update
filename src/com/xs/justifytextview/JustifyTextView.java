package com.xs.justifytextview;

import java.lang.Character.UnicodeBlock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * @update 2015-08-15
 * @author dalu
 *
 */
public class JustifyTextView extends TextView {

	private Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");;
	private Matcher matcher;
	/** 文本 */
	private SpannedString mText;

	/** 画笔 */
	private Paint mPaint;

	/** 文本宽度 */
	private int textWidth;

	/** 行距 */
	private float lineSpacing;

	public JustifyTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public JustifyTextView(Context context) {
		this(context, null);
	}

	public float mBaikeTextHeight = 0;// 文本高度
	public int mFontHeight = 0;

	public JustifyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		/**
		 * 获得我们所定义的自定义样式属性,行距lineSpacing
		 */
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.JustifyTextView, defStyle, 0);
		int n = a.getIndexCount();
		int attr;
		for (int i = 0; i < n; i++) {
			attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.JustifyTextView_lineSpacing:
				lineSpacing = a.getDimensionPixelSize(attr, (int) TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_SP, 3,
								getResources().getDisplayMetrics()));
				break;
			}

		}
		a.recycle();

		// TODO 获取原TextView的画笔,保持原属性不变
		mPaint = this.getPaint();
		// 获取文本颜色设置给画笔
		// mPaint.setColor(this.getCurrentTextColor());
		mPaint.setColor(Color.BLACK);
	}

	/** 单词单元数组,主要针对英文 */
	private String[] words;

	private void arrayTowords() {
		char[] array;
		array = mText.toString().toCharArray();
		int j = 0;
		words = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			words[i] = "";
			if (array[i] >= 0 && array[i] < 0x7f) {
				if (String.valueOf(array[i]).equals("\n")) {
					j++;
					words[j] = "\n";
					j++;
					continue;
				}
				words[j] = words[j] + (array[i] + "").trim();
				if (array.length - 1 > i + 1
						&& (array[i + 1] == ' ' || array[i + 1] == ' ')) {
					j++;
				}
			} else {
				if (String.valueOf(array[i]).equals("\n")) {
					j++;
					words[j] = "\n";
					j++;
					continue;
				}
				words[j] = words[j] + (array[i] + "").trim();
				UnicodeBlock ub = Character.UnicodeBlock.of((array[i + 1]));
				if (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
						|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
						|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
						|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
						|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS) {
					continue;
				}
				j++;
			}
		}

	}

	/**
	 * @return lines-int 重新排版后文档的行数
	 */
	private int getLines() {
		float linewidth = 0;
		int line = 0;
		float measureText = 0;
		float blankwidth = mPaint.measureText(" ");
		for (int i = 0; i < words.length; i++) {
			measureText = mPaint.measureText(words[i]);
			if (linewidth + measureText >= textWidth) {
				if (words[i].isEmpty() || words[i] == "")
					continue;
				line++;
				linewidth = 0;
				i--;
			} else {
				if (String.valueOf(words[i]).equals("\n")) {
					linewidth = textWidth;
				}
				if (mPaint.measureText(words[i]) != mPaint.measureText("中")) {
					linewidth += (measureText + blankwidth);
				} else {
					linewidth += measureText;
				}
			}
		}
		return line + 1;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		float linewidth = 0;
		int point = 0;
		int line = 0;
		String content = getText().toString();
		String tmp = content;
		ForegroundColorSpan[] spans;
		float widthPoint = 0;
		int lastindex = 0;
		float measureText = 0;
		float blankwidth = mPaint.measureText(" ");
		for (int i = 0; i < words.length; i++) {
			measureText = mPaint.measureText(words[i]);
			if ((linewidth + measureText >= textWidth)
					|| (i == words.length - 1)) {
				widthPoint = 0;
				for (int k = point; k < i; k++) {
					if (String.valueOf(words[k]).equals("\n")) {
					} else {
						if (String.valueOf(words[k]).equals("")) {
							++lastindex;
						} else if (String.valueOf(words[k]).equals(" ")
								|| tmp.indexOf(String.valueOf(words[k])) == -1) {
							++lastindex;
						} else {
							lastindex += tmp.indexOf(String.valueOf(words[k]));
							spans = mText.getSpans(lastindex, lastindex
									+ words[k].length(),
									ForegroundColorSpan.class);
							if (spans != null && spans.length > 0) {
								mPaint.setColor(spans[0].getForegroundColor());
							} else {
								matcher = pattern.matcher(words[k]);
								if (matcher.find()) {
									spans = mText.getSpans(lastindex, lastindex
											+ words[k].length() - 1,
											ForegroundColorSpan.class);
								}
								if (spans != null && spans.length > 0) {
									mPaint.setColor(spans[0]
											.getForegroundColor());
								} else {
									mPaint.setColor(Color.BLACK);
								}
							}
							lastindex += words[k].length();
						}
						canvas.drawText(words[k],
								widthPoint + getPaddingLeft(),
								(float) (mPaint.getTextSize() + lineSpacing)
										* (line + 1) + getPaddingTop(), mPaint);
						if (lastindex < content.length())
							tmp = content.substring(lastindex);
					}
					if (i != words.length - 1) {
						widthPoint = widthPoint + mPaint.measureText(words[k])
								+ ((textWidth - linewidth) / (i - point - 1));
					} else {
						widthPoint = widthPoint + mPaint.measureText(words[k]);
					}
					if (mPaint.measureText(words[k]) != mPaint.measureText("过")) {
						widthPoint += blankwidth;
					}
				}
				if (i != words.length - 1) {
					line++;
					point = i;
					linewidth = 0;
					widthPoint = 0;
					i--;
				}
			} else {
				// 逐个单词累计,长度够一行绘制一次or换行
				if (String.valueOf(words[i]).equals("\n")) {
					linewidth = textWidth;
				}
				// 英文每个单词后面有一个空格
				if (mPaint.measureText(words[i]) != mPaint.measureText("中")) {
					linewidth += (measureText + blankwidth);
				} else {
					linewidth += measureText;
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 此处得到的是TextView的宽度;高度需重新计算
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		// 减去左右文本边距的文本区域宽度
		textWidth = widthSize - getPaddingLeft() - getPaddingRight();
		int height = 1000;// TextView高度

		// 获取text,分析构造单词数组,并计算出行数
		mText = SpannedString.valueOf(this.getText());

		arrayTowords();
		int lines = getLines();

		height = (int) (lines * (mPaint.getTextSize() + lineSpacing))
				+ (int) lineSpacing;// 0.8偏大
		setMeasuredDimension(widthSize, height + getPaddingBottom());
	}

}
