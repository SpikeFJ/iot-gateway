import Detail from './detail.js'

export default {
    data() {
        return {
            online_list: [],
            query: "",
            dateTimeRange: '',

            totalPages: 0,
            totalRecords: 0,
            pageSize: 10,
            pageNum: 1,

        }
    },
    components:{
        Detail
    },
    mounted() {
        this.search();
    },
    methods: {
        test() {
            this.online_list = [
                { connect_time: "2023-09-27 10:02:21", remote_ip: "127.0.0.1:11953", receive_packets: 1, send_packets: 2, receive_time: "2023-09-27 10:02:25", device_id: "T0000000000000016", bussiness_id: "10086" },
                { connect_time: "2023-09-27 10:02:21", remote_ip: "127.0.0.1:11953", receive_packets: 1, send_packets: 2, receive_time: "2023-09-27 10:02:25", device_id: "T0000000000000017", bussiness_id: "10086" },
                { connect_time: "2023-09-27 10:02:21", remote_ip: "127.0.0.1:11953", receive_packets: 1, send_packets: 2, receive_time: "2023-09-27 10:02:25", device_id: "T0000000000000018", bussiness_id: "10086" },
                { connect_time: "2023-09-27 10:02:21", remote_ip: "127.0.0.1:11953", receive_packets: 1, send_packets: 2, receive_time: "2023-09-27 10:02:25", device_id: "T0000000000000019", bussiness_id: "10086" },
                { connect_time: "2023-09-27 10:02:21", remote_ip: "127.0.0.1:11953", receive_packets: 1, send_packets: 2, receive_time: "2023-09-27 10:02:25", device_id: "T0000000000000020", bussiness_id: "10086" },
                { connect_time: "2023-09-27 10:02:21", remote_ip: "127.0.0.1:11953", receive_packets: 1, send_packets: 2, receive_time: "2023-09-27 10:02:25", device_id: "T0000000000000021", bussiness_id: "10086" },
                { connect_time: "2023-09-27 10:02:21", remote_ip: "127.0.0.1:11953", receive_packets: 1, send_packets: 2, receive_time: "2023-09-27 10:02:25", device_id: "T0000000000000022", bussiness_id: "10086" },
            ]
        },
        search() {
            this.online_list = [];
            var config = {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                },
                body: JSON.stringify({
                    query: this.query,
                    pageSize: this.pageSize,
                    pageNum: this.pageNum
                })
            };

            fetch('/tcp/onlineList', config)
                .then(resp => resp.json())
                .then(res => {
                    if (res.code != 1) {

                    } else {
                        this.totalPages = res.data.totalPages;
                        this.totalRecords = res.data.totalRecords;

                        res.data.data.forEach(element => {
                            var tmp = {};
                            tmp.connect_time = element.createTime;
                            tmp.remote_ip = element.remoteAddress;
                            tmp.receive_packets = element.receivedPackets;
                            tmp.send_packets = element.sendPackets;
                            tmp.receive_time = element.lastReadTime;
                            tmp.device_id = element.deviceId;
                            tmp.bussiness_id = element.bussinessId;

                            this.online_list.push(tmp);
                        });
                    }
                });
        },
        prePage() {
            this.pageNum -= 1;
            if (this.pageNum < 0)
                this.pageNum = 1;
            this.search();
        },
        nextPage() {
            this.pageNum += 1;
            this.search();
        },
        goto(e) {
            this.pageNum = parseInt(e.target.innerHTML);
            this.search();
        },
        displayIndex(index) {
            if (this.pageNum <= 1)
                return index + 1;
            else {
                return (this.pageNum - 1) * this.pageSize + index + 1;
            }
        },
        detail(newDeviceId){
            this.$refs.child.query(newDeviceId);
        },
        pagination() {
            var obj = [];
            if (this.pageNum > this.totalPages) {
                this.pageNum = this.totalPages;
            }
            let from = (this.pageNum <= 3 ? 1 : this.pageNum - 3);
            let to = from + 3;

            for (let startIndex = from; startIndex < to; startIndex++) {
                var classStr = startIndex == this.pageNum ?
                    "bg-blue-50 border border-gray-300 text-blue-600 hover:bg-blue-100 hover:text-blue-700  py-2 px-3 dark:border-gray-700 dark:bg-gray-700 dark:text-white"
                    : "g-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 ml-0 rounded-l-lg leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white";

                obj.push({
                    name: startIndex,
                    class: classStr,
                    action: this.goto
                })
            }
            return obj;
        }
    },
    template: `
<div class="h-4/6">
<div class="flex flex-wrap">
    <div class="md:w-3/4 w-full text-right px-1">
    </div>    
    <div class="md:w-1/4 w-full text-right">
      <input type="text" v-model="query" @keyup.enter='search' placeholder="设备Id/业务Id/Ip端口" class="input input-bordered w-1/8 max-w-sm input-sm" />
      <button class="btn btn-outline btn-sm mx-3 px-6 btn-primary" @click="search" >查找</button>
    </div>
</div>

<div class="overflow-auto h-5/6">
<table class="table">
<thead>
  <tr>
    <th class="text-center">序号</th>
    <th class="text-center">连接时间</th>
    <th class="text-center">远程IP端口</th>
    <th class="text-center">发送数据包</th>
    <th class="text-center">接收数据包</th>
    <th class="text-center">最后一次接收时间</th>
    <th class="text-center">唯一标识</th>
    <th class="text-center">业务id</th>
    <th class="text-center">操作</th>
  </tr>
</thead>
<tbody>
  <tr v-for="(item, index) in online_list" :key="item.device_id">
    <th class="text-center">{{displayIndex(index)}}</th>
    <td class="text-center">{{item.connect_time}}</td>
    <td class="text-center">{{item.remote_ip}}</td>
    <td class="text-center">{{item.receive_packets}}</td>
    <td class="text-center">{{item.send_packets}}</td>
    <td class="text-center">{{item.receive_time}}</td>
    <td class="text-center">{{item.device_id}}</td>
    <td class="text-center">{{item.bussiness_id}}</td>
    <td class="text-center">
      <label for="my_modal_7" class="link link-primary" @click='detail(item.device_id)'>详情</label>
    </td>
  </tr>
</tbody>
</table>
</div>
<footer class="footer my-3 p-1 justify-end">
<aside>
<!-- This is an example component -->
<div class="max-w-2xl mx-auto">
  <nav aria-label="Page navigation example">
    <ul class="inline-flex -space-x-px">
      <li>
        <a class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 ml-0 rounded-l-lg leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white" @click='prePage'>上一页</a>
      </li>
      <li v-for="item in pagination()">
        <a @click='item.action' :class='item.class'>{{item.name}}</a>
      </li>
      <li>
        <a class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 rounded-r-lg leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white" @click='nextPage'>下一页</a>
      </li>
      <li class='px-5'>
        每页 {{pageSize}} 条,共 {{totalPages}} 页,计 {{totalRecords}} 条记录
      </li>
    </ul>
  </nav>
</div>
</aside>
</footer>
</div>

<input type="checkbox" id="my_modal_7" class="modal-toggle" />
<div class="modal ">
  <div class="modal-box w-11/12 max-w-5xl">
  <h1 class="sm:text-3xl text-2xl font-medium text-center title-font text-gray-900 mb-4">会话详情</h1>
    <Detail ref='child' />
  </div>
  <label class="modal-backdrop" for="my_modal_7">Close</label>
</div>
`
}