export default {
    data() {
        return {
            online_list: [],
            query: "",
            dateTimeRange:''
        }
    },
    mounted() {
        this.search();
    },
    methods: {
        search() {
            this.online_list = [];
            var config = {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                },

            };
            if (typeof this.query && "undefined" && this.query != null && this.query.trim() != "") {
                var param = {
                    query: this.query
                }
                config.body = JSON.stringify(param)
            };
            fetch('/tcp/onlineList', config)
                .then(resp => resp.json())
                .then(res => {
                    if (res.code != 1) {

                    } else {
                        res.data.forEach(element => {
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
        detail(id) {

        }
    },
    template: `
<div class="h-3/4">
  <div class="flex flex-wrap -m-4">
      <div class="md:w-3/4 w-full text-right px-1">
      </div>    
      <div class="md:w-1/4 w-full text-right">
        <input type="text" v-model="query" placeholder="设备Id/业务Id/Ip" class="input input-bordered w-1/8 max-w-sm input-sm" />
        <button class="btn btn-outline btn-sm mx-3 btn-primary" @click="search">查找</button>
      </div>
  </div>

  <table class="table table-pin-rows table-pin-cols m-5 overflow-auto h-2/3 ">
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
      <th class="text-center">实时</th>
      <th class="text-center">操作</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="(item, index) in online_list" :key="item.device_id">
      <th class="text-center">{{index+1}}</th>
      <td class="text-center">{{item.connect_time}}</td>
      <td class="text-center">{{item.remote_ip}}</td>
      <td class="text-center">{{item.receive_packets}}</td>
      <td class="text-center">{{item.send_packets}}</td>
      <td class="text-center">{{item.receive_time}}</td>
      <td class="text-center">{{item.device_id}}</td>
      <td class="text-center">{{item.bussiness_id}}</td>
      <td class="text-center">
        <input type="checkbox" class="toggle toggle-primary toggle-xs" v-model="item.notify"/>
      </td>
      <td class="text-center">
        <a class="link link-primary" @click='detail(item.device_id)'>详情</a>
      </td>
    </tr>
  </tbody>
  </table>

<footer class="footer footer-center p-4 text-base-content">
  <aside>
  <!-- This is an example component -->
  <div class="max-w-2xl mx-auto">
  
    <nav aria-label="Page navigation example">
      <ul class="inline-flex -space-x-px">
        <li>
          <a href="#"
            class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 ml-0 rounded-l-lg leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">Previous</a>
        </li>
        <li>
          <a href="#"
            class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">1</a>
        </li>
        <li>
          <a href="#"
            class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">2</a>
        </li>
        <li>
          <a href="#" aria-current="page"
            class="bg-blue-50 border border-gray-300 text-blue-600 hover:bg-blue-100 hover:text-blue-700  py-2 px-3 dark:border-gray-700 dark:bg-gray-700 dark:text-white">3</a>
        </li>
        <li>
          <a href="#"
            class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">4</a>
        </li>
        <li>
          <a href="#"
            class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">5</a>
        </li>
        <li>
          <a href="#"
            class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-100 hover:text-gray-700 rounded-r-lg leading-tight py-2 px-3 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white">Next</a>
        </li>
      </ul>
    </nav>
  </div>
  </aside>
</footer>
</div>
`
}