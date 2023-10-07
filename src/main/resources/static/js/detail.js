export default {
  props: ["id"],
  emits: ['update:id'],
  data() {
    return {
      basic: {},
      history: []
    }
  },
  watch: {
    id(newValue, oldValue) {
      this.query(newValue);
    }
  },
  methods: {
    test(id) {
      console.log("设备Id:" + id);
      this.basic = {
        sessionStatus: "已登录",
        remoteAddress: "127.0.0.1",
        createTime: "yyyy-MM-dd HH:mm:ss",
        channelId: "通道编号",
        deviceId: "T0000000000001",
        bId: "T0000000000002",

        sendPackets: "T0000000000001",
        sendBytes: "T0000000000002",
        lastWriteTime: "T0000000000002",

        receivedPackets: "T0000000000001",
        receivedBytes: "T0000000000002",
        lastReadTime: "T0000000000002",
      }
      this.history = [
        { dataType: 0, data: "", time: "2023-10-07 12:00:01" },
        { dataType: 1, data: "", time: "2023-10-07 12:00:01" },
        { dataType: 1, data: "", time: "2023-10-07 12:00:01" },
        { dataType: 2, data: "", time: "2023-10-07 12:00:01" },
        { dataType: 2, data: "", time: "2023-10-07 12:00:01" },
        { dataType: 1, data: "", time: "2023-10-07 12:00:01" },
        { dataType: 2, data: "", time: "2023-10-07 12:00:01" },
        { dataType: 2, data: "", time: "2023-10-07 12:00:01" }
      ];
    },
    parseType(type) {
      if (type == 0) {
        return "连接"
      } if (type == 1) {
        return "接收"
      } if (type == 2) {
        return "发送"
      }
    },
    query(id) {
      var config = {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json;charset=utf-8'
        },
        body: JSON.stringify({
          id: id
        })
      };
      fetch('/single', config)
        .then(resp => resp.json())
        .then(res => {
          if (res.code != 1) {

          } else {
            this.basic = res.data.basic;
            this.history = res.data.basic;
          }
        });
    }
  },
  template: `
  <h2 class="text-indigo-500 text-lg title-font font-medium mb-1">基础信息</h2>
  <hr/>
  <dl class="flex items-center space-x-6 px-3 py-1">
    <div class='w-32 h-14'>
        <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">会话状态</dt>
        <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">已登陆</dd>
    </div>
    <div class='w-32 h-14'>
        <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">远程地址</dt>
        <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.remoteAddress}}</dd>
    </div>
    <div class='w-32 h-14'>
        <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">设备编号</dt>
        <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.deviceId}}</dd>
    </div>
    <div class='w-32 h-14'>
        <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">业务编号</dt>
        <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.bId}}</dd>
    </div>
    <div class='w-48 h-14'>
        <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">创建时间</dt>
        <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.createTime}}</dd>
    </div>
  </dl> 

  <dl class="flex items-center space-x-6 px-3 py-3">
  <div class='w-32 h-14'>
      <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">接收包数</dt>
      <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.receivedPackets}}</dd>
  </div>
  <div class='w-32 h-14'>
      <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">接收字节数</dt>
      <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.receivedBytes}}</dd>
  </div>
  <div class='w-48 h-14'>
      <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">最后接收时间</dt>
      <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.lastReadTime}}</dd>
  </div>
  <div class='w-32 h-14'>
      <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">发送包数</dt>
      <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sendPackets}}</dd>
  </div>
  <div class='w-32 h-14'>
      <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">发送字节数</dt>
      <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.sendBytes}}</dd>
  </div>
  <div class='w-48 h-14'>
      <dt class="mb-2 font-semibold leading-none text-gray-900 dark:text-white">最后发送时间</dt>
      <dd class="mb-4 font-light text-gray-500 sm:mb-5 dark:text-gray-400">{{basic.lastWriteTime}}</dd>
  </div>
</dl> 
  <h2 class="text-indigo-500 text-lg title-font font-medium mb-1">历史连接</h2>
  <hr/>
  <div class="overflow-auto h-4/5">
  <table class="table">
  <thead>
    <tr>
      <th class="text-center w-5">序号</th>
      <th class="text-center w-2/6">发生时间</th>
      <th class="text-center w-20">类型</th>
      <th class="text-center ">数据</th>
      <th class="text-center w-20">操作</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="(item, index) in history" :key="item.device_id" :class="{'bg-green-200':item.dataType==0,'bg-orange-200':item.dataType==1,'bg-sky-200':item.dataType==2}">
      <td class="text-center w-5">{{index+1}}</td>
      <td class="text-center w-2/6">{{item.time}}</td>
      <td class="text-center w-20">{{parseType(item.dataType)}}</td>
      <td class="text-center">{{item.data}}</td>
      <td class="text-center w-20">
        <button class="btn btn-sm btn-outline p-1">复制</button>
      </td>
    </tr>
  </tbody>
  </table>
</div>
    `
}