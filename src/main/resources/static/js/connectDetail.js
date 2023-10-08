export default {
    data() {
        return {
            basic: {},
            history: []
        }
    },
    methods: {
        parseType(type) {
            if (type == 0) {
                return "连接"
            } if (type == 1) {
                return "接收"
            } if (type == 2) {
                return "发送"
            }else{
                return "未知"
            }
        },
        query(connectId) {
            var config = {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                },
                body: JSON.stringify({
                    id: connectId
                })
            };
            fetch('/singleConnect', config)
                .then(resp => resp.json())
                .then(res => {
                    if (res.code != 1) {

                    } else {
                        this.basic = res.data.basic;
                        this.history = res.data.history;
                    }
                });
        },
        copyTo(value) {
            let tempInput = document.createElement('input');//创建input元素
            document.body.appendChild(tempInput);//向页面底部追加输入框
            tempInput.setAttribute('value', value);//添加属性，将url赋值给input元素的value属性
            tempInput.select();//选择input元素
            document.execCommand("Copy");//执行复制命令
            //复制之后再删除元素，否则无法成功赋值
            tempInput.remove();//删除动态创建的节点
        }
    },
    template: `
  
  <h2 class="text-indigo-500 text-lg title-font font-medium mb-1">基础信息</h2>
  <hr/>
    <div class="flex flex-wrap">
      <div class="xl:w-1/4 lg:w-1/2 md:w-full px-1 py-1 border-l-2 border-gray-200 border-opacity-60">
          <dl class="flex items-center space-x-6 px-3 py-3">
            <div class='w-32 h-14'>
                <dt class="mb-2  leading-none text-gray-900 dark:text-white">会话状态</dt>
                <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sessionStatus}}</dd>
            </div>
            <div class='w-32 h-14'>
                <dt class="mb-2  leading-none text-gray-900 dark:text-white">远程地址</dt>
                <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.remoteAddress}}</dd>
            </div>
            <div class='w-48 h-14'>
                <dt class="mb-2  leading-none text-gray-900 dark:text-white">创建时间</dt>
                <dd class="mb-4 whitespace-nowrap font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.createTime}}</dd>
            </div>
          </dl> 
      </div>
      <div class="xl:w-1/4 lg:w-1/2 md:w-full px-1 py-1 border-l-2 border-gray-200 border-opacity-60">
        <dl class="flex items-center space-x-6 px-3 py-3">
          <div class='w-32 h-14'>
              <dt class="mb-2  leading-none text-gray-900 dark:text-white">接收包数</dt>
              <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.receivedPackets}}</dd>
          </div>
          <div class='w-32 h-14'>
              <dt class="mb-2  leading-none text-gray-900 dark:text-white">接收字节数</dt>
              <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.receivedBytes}}</dd>
          </div>
          <div class='w-48 h-14'>
              <dt class="mb-2  leading-none text-gray-900 dark:text-white">最后接收时间</dt>
              <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.lastReadTime}}</dd>
          </div>
        </dl> 
      </div>
      <div class="xl:w-1/4 lg:w-1/2 md:w-full px-1 py-1 border-l-2 border-gray-200 border-opacity-60">
        <dl class="flex items-center space-x-6 px-3 py-3">
          <div class='w-48 h-14'>
                <dt class="mb-2  leading-none text-gray-900 dark:text-white">设备编号</dt>
                <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.deviceId}}</dd>
            </div>
            <div class='w-48 h-14'>
                <dt class="mb-2  leading-none text-gray-900 dark:text-white">业务编号</dt>
                <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.bId}}</dd>
            </div>
          </dl> 
      </div>
      <div class="xl:w-1/4 lg:w-1/2 md:w-full px-1 py-1 border-l-2 border-gray-200 border-opacity-60">
        <dl class="flex items-center space-x-6 px-3 py-3">
          <div class='w-32 h-14'>
              <dt class="mb-2  leading-none text-gray-900 dark:text-white">发送包数</dt>
              <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sendPackets}}</dd>
          </div>
          <div class='w-32 h-14'>
              <dt class="mb-2  leading-none text-gray-900 dark:text-white">发送字节数</dt>
              <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sendBytes}}</dd>
          </div>
          <div class='w-48 h-14'>
              <dt class="mb-2  leading-none text-gray-900 dark:text-white">最后发送时间</dt>
              <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.lastWriteTime}}</dd>
          </div>
        </dl> 
      </div>
    </div>
  <h2 class="text-indigo-500 text-lg title-font font-medium mb-1">历史数据</h2>
  <hr/>
  <div class="h-4/5">
  <table class="table table-sm">
  <thead>
    <tr>
      <th class="text-center whitespace-nowrap w-5">序号</th>
      <th class="text-center whitespace-nowrap w-10">发生时间</th>
      <th class="text-center whitespace-nowrap w-5">类型</th>
      <th class="text-center whitespace-nowrap">数据</th>
      <th class="text-center whitespace-nowrap w-10">操作</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="(item, index) in history" :key="item.device_id" >
      <td class="text-center whitespace-nowrap w-5">{{index+1}}</td>
      <td class="text-center whitespace-nowrap w-10">{{item.time}}</td>
      <td class="text-center whitespace-nowrap w-5" :class="{'text-error':item.dataType==0,'text-info':item.dataType==1,'text-warning':item.dataType==2}">{{parseType(item.dataType)}}</td>
      <td class="text-left">
        <p class="w-11/12 overflow-hidden whitespace-nowrap overflow-ellipsis">
          {{item.data}}
        </p>
      </td>
      <td class="text-center whitespace-nowrap w-10">
        <button class="btn btn-sm btn-outline p-1" @click="copyTo(item.data)">复制</button>
      </td>
    </tr>
  </tbody>
  </table>
</div>
    `
}