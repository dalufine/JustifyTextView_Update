package com.xs.justifytextview;

import java.lang.Character.UnicodeBlock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * ���˷�ɢ�����TextView,֧����Ӣ�Ļ��</br> �����ʹ��������Զ������������о�,�������Ժ�ԭ��TextViewһֱ������Ч</br>
 * xmlns:custom="http://schemas.android.com/apk/res/com.xs.justifytextview"<br>
 * custom:lineSpacing="10sp"<br>
 * 
 * @author xsing
 * 
 */
public class JustifyTextView_bak extends TextView
{
	private static final String TAG = "JustifyTextView";
	/** �ı� */
	private String mText;

	/** ���� */
	private Paint mPaint;

	/** �ı���� */
	private int textWidth;

	/** �о� */
	private float lineSpacing;

	public JustifyTextView_bak(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public JustifyTextView_bak(Context context)
	{
		this(context, null);
	}

	public float mBaikeTextHeight = 0;// �ı��߶�
	public int mFontHeight = 0;

	public JustifyTextView_bak(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		/**
		 * ���������������Զ�����ʽ����,�о�lineSpacing
		 */
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.JustifyTextView, defStyle, 0);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++)
		{
			int attr = a.getIndex(i);
			switch (attr)
			{
			case R.styleable.JustifyTextView_lineSpacing:
				lineSpacing = a.getDimensionPixelSize(attr, (int) TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_SP, 3,
								getResources().getDisplayMetrics()));
				break;
			}

		}
		a.recycle();

		// TODO ��ȡԭTextView�Ļ���,����ԭ���Բ���
		mPaint = this.getPaint();
		// ��ȡ�ı���ɫ���ø�����
		mPaint.setColor(this.getCurrentTextColor());
	}

	/** ���ʵ�Ԫ����,��Ҫ���Ӣ�� */
	private String[] words;

	private void arrayTowords()
	{
		char[] array = mText.toCharArray();
		int j = 0;
		words = new String[array.length];
		for (int i = 0; i < array.length; i++)
		{
			words[i] = "";
			if(array[i] >= 0 && array[i] < 0x7f)
			{
				if(String.valueOf(array[i]).equals("\n"))
				{
					j++;
					words[j] = "\n";
					j++;
					continue;
				}
				words[j] = words[j] + (array[i] + "").trim();
				if(array.length - 1 > i + 1
						&& (array[i + 1] == ' ' || array[i + 1] == ' '))
				{
					j++;
				}

			} else
			{
				if(String.valueOf(array[i]).equals("\n"))
				{
					j++;
					words[j] = "\n";
					j++;
					continue;
				}
				words[j] = words[j] + (array[i] + "").trim();
				UnicodeBlock ub = Character.UnicodeBlock.of((array[i + 1]));
				if(ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
						|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
						|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
						|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
						|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS)
				{
					continue;
				}
				j++;
			}
		}

	}

	/**
	 * @return lines-int �����Ű���ĵ�������
	 */
	private int getLines()
	{
		float linewidth = 0;
		int line = 0;
		float blankwidth = mPaint.measureText(" ");
		for (int i = 0; i < words.length; i++)
		{
			float measureText = mPaint.measureText(words[i]);

			if(linewidth + measureText >= textWidth)
			{
				if(words[i].isEmpty() || words[i] == "")
					break;
				line++;
				linewidth = 0;
				i--;
			} else
			{
				if(String.valueOf(words[i]).equals("\n"))
				{
					linewidth = textWidth;
				}
				if(mPaint.measureText(words[i]) != mPaint.measureText("��"))
				{
					linewidth += (measureText + blankwidth);
				} else
				{
					linewidth += measureText;
				}
			}
		}
		return line + 1;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// super.onDraw(canvas);
		Log.d(TAG, "==============onDraw");
		float linewidth = 0;
		int point = 0;
		int line = 0;
		float blankwidth = mPaint.measureText(" ");
		for (int i = 0; i < words.length; i++)
		{
			float measureText = mPaint.measureText(words[i]);

			if(linewidth + measureText >= textWidth)
			{
				float widthPoint = 0;
				for (int k = point; k < i; k++)
				{

					if(String.valueOf(words[k]).equals("\n"))
					{

					} else
					{
						// TODO ����,����������Ƶ���word
						canvas.drawText(words[k],
								widthPoint + getPaddingLeft(),
								(float) (mPaint.getTextSize() + lineSpacing)
										* (line + 1) + getPaddingTop(), mPaint);
					}
					widthPoint = widthPoint + mPaint.measureText(words[k])
							+ ((textWidth - linewidth) / (i - point - 1));
					// �����������,����һ���ո�
					if(mPaint.measureText(words[k]) != mPaint.measureText("��"))
					{
						widthPoint += blankwidth;
					}
				}
				// if(words[i]);
				line++;
				point = i;
				linewidth = 0;
				widthPoint = 0;
				i--;
			} else
			{ // ��������ۼ�,���ȹ�һ�л���һ��or����
				if(String.valueOf(words[i]).equals("\n"))
				{
					linewidth = textWidth;
				}
				// Ӣ��ÿ�����ʺ�����һ���ո�
				if(mPaint.measureText(words[i]) != mPaint.measureText("��"))
				{
					linewidth += (measureText + blankwidth);
				} else
				{
					linewidth += measureText;
				}
			}
		}
		Log.d(TAG, "lines=====ondraw" + line);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		Log.d(TAG, "==============onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// �˴��õ�����TextView�Ŀ��;�߶������¼���
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int width = widthSize;
		// ��ȥ�����ı��߾���ı�������
		textWidth = widthSize - getPaddingLeft() - getPaddingRight();
		int height = 1000;// TextView�߶�

		// ��ȡtext,�������쵥������,�����������
		mText = (String) this.getText();
		arrayTowords();
		int lines = getLines();
		Log.d(TAG, "lines" + lines);

		float fontSpacing = mPaint.getFontSpacing();// �Ƽ��м��
		FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
		int fontheight = fontMetricsInt.bottom - fontMetricsInt.top;

		height = (int) (lines * (mPaint.getTextSize() + lineSpacing));// 0.8ƫ��
		Log.d(TAG,
				"width" + width + "  height:" + height + " fontheight:"
						+ fontheight + " textSize:" + mPaint.getTextSize()
						+ " fontSpacing:" + fontSpacing + "mPaint����:"
						+ mPaint.getColor());
		setMeasuredDimension(widthSize, height + getPaddingBottom());
	}

}
