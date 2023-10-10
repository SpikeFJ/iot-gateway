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
   <div class="py-1 flex flex-wrap md:flex-nowrap h-1/3 overflow-hidden">
  <div class="mr-12 md:w-48 md:mb-0 mb-6 flex-shrink-0 flex flex-col">
    <span class="text-indigo-500 text-lg title-font font-medium mb-1">基本信息</span>
    <span class="mt-1 text-gray-500 text-sm">
      <div class="grid grid-cols-4 whitespace-nowrap">
        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">会话状态</div>
        <div class="w-14 h-1 col-span-2  font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sessionStatus}}</div>
        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">远程地址</div>
        <div class="w-14 h-1 col-span-2   font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.remoteAddress}}</div>
        <div class="w-14 h-1  col-span-2 text-gray-900 dark:text-white">创建时间</div>
        <div class="w-14 h-1 col-span-2  whitespace-nowrap font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.createTime}}</div>

        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">接收包</div>
        <div class="w-14 h-1 col-span-2 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.receivedPackets}}</div>
        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">接收字节</div>
        <div class="w-14 h-1 col-span-2 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.receivedBytes}}</div>
        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">接收时间</div>
        <div class="w-14 h-1 col-span-2 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.lastReadTime}}</div>

        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">发送包</div>
        <div class="w-14 h-1 col-span-2 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sendPackets}}</div>
        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">发送字节</div>
        <div class="w-14 h-1 col-span-2 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sendBytes}}</div>
        <div class="w-14 h-1 col-span-2 text-gray-900 dark:text-white">发送时间</div>
        <div class="w-14 h-1 col-span-2 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.lastWriteTime}}</div>
      </div>
    </span>
  </div>
  <div class="md:flex-grow w-2/3">
    <h2 class="text-indigo-500 text-lg title-font font-medium mb-1">收发记录</h2>
    <div class="overflow-auto h-5/6 w-full">
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
              <div class="w-96 overflow-hidden whitespace-nowrap overflow-ellipsis">
                {{item.data}}
              </div>
            </td>
            <td class="text-center whitespace-nowrap w-10">
              <button class="btn btn-sm btn-outline p-1" @click="copyTo(item.data)">复制</button>
            </td>
          </tr>
        </tbody>
        </table>
    </div>
    
  </div>
</div>
    `
}