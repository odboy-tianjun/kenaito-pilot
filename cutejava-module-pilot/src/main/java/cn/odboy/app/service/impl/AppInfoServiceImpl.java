/*
 * Copyright 2021-2026 Odboy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.odboy.app.service.impl;

import cn.odboy.app.dal.dataobject.AppInfoTb;
import cn.odboy.app.dal.mysql.AppInfoMapper;
import cn.odboy.app.service.AppInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用信息 服务实现类
 * </p>
 *
 * @author odboy
 * @since 2026-03-27
 */
@Service
public class AppInfoServiceImpl extends ServiceImpl<AppInfoMapper, AppInfoTb> implements AppInfoService {

}
