<?php

namespace AlibabaCloud\OpenApiUtil;

class SmallJHandler extends JHandler
{
    /** @var int j的最大可用值 */
    const SMALLEST_J = 0;
    /** @var int j的最小可用值 */
    const BIGGEST_J = 15;
    /** @var string T常量 */
    const T = '79cc4519';

    /**
     * 补充父类
     * SmallJHandler constructor.
     */
    public function __construct()
    {
        parent::__construct(self::T, self::SMALLEST_J, self::BIGGEST_J);
    }

    /**
     * 布尔函数.
     *
     * @param $X Word 长度32的比特串
     * @param $Y Word 长度32的比特串
     * @param $Z Word 长度32的比特串
     *
     * @return Word
     */
    public function FF($X, $Y, $Z)
    {
        return self::boolFunction($X, $Y, $Z);
    }

    /**
     * 布尔函数.
     *
     * @param $X Word 长度32的比特串
     * @param $Y Word 长度32的比特串
     * @param $Z Word 长度32的比特串
     *
     * @return Word
     */
    public function GG($X, $Y, $Z)
    {
        return self::boolFunction($X, $Y, $Z);
    }

    /**
     * 小j值的布尔函数公共方法.
     *
     * @param $X Word 长度32的比特串
     * @param $Y Word 长度32的比特串
     * @param $Z Word 长度32的比特串
     *
     * @return Word
     */
    private static function boolFunction($X, $Y, $Z)
    {
        return WordConversion::xorConversion(
            [
                $X,
                $Y,
                $Z,
            ]
        );
    }
}
