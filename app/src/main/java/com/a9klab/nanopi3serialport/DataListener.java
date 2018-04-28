/*
 *
 *  *
 *  *  * Created by hex
 *  *  * Created on 2016
 *  *  * Copyright (c) 深圳半岛医疗有限公司  All Rights Reserved.
 *  *  *
 *  *  * This software is the confidential and proprietary information of peninsulalaser.com.
 *  *  * You shall not disclose such Confidential Information and shall use it only in
 *  *  * accordance with the terms of the license agreement you entered into
 *  *  * with www.peninsulalaser.com.
 *  *
 *
 */

package com.a9klab.nanopi3serialport;

import android.os.Bundle;

/**
 * com.peninsulalaser.lib
 *
 * @author hex
 *         Created on 2016/9/19 12:02
 * @version 1.0
 *          ============================================================================
 *          版权所有 2014-2015 重庆德马光电有限公司深圳分公司,并保留所有权利。
 *          ----------------------------------------------------------------------------
 *          提示：在未取得重庆德马光电有限公司深圳分公司授权之前,您不能将本软件中的代码进行
 *          传播 、分发、共享, 否则重庆德马光电有限公司深圳分公司将保留追究的权力。
 *          ----------------------------------------------------------------------------
 *          官方网站：http://www.peninsulalaser.com
 *          ============================================================================
 */
public interface DataListener {
    public void getData(byte[] data);
}
