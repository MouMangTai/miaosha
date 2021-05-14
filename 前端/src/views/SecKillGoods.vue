<template>
	<div>
		<h2>商品秒杀详情页面</h2>

		<el-row :gutter="20">
			<el-col :span="12">
				<div>
					<img src="https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png"
						class="image" />
				</div>
			</el-col>
			<el-col :span="12">
				<div>
					
					<el-row :gutter="20">
					  <el-col :span="24"><div class="divclass title">{{title}}</div></el-col>
					  <el-col :span="24"><div class="divclass ">{{info}}</div></el-col>
					  <el-col :span="24"><div class="divclass price">￥{{price}}</div></el-col>
					  <el-col :span="24"><div class="divclass ">库存{{save}}</div></el-col>
						<el-col :span="24"><div class="divclass "><times :overTime="overTime" :overFunc="overFunc"/></div></el-col>
						<el-col :span="24">
							<div class="divclass ">
								<el-button v-if="flag" type="danger" @click="qiangGou()">立即抢购</el-button>
								<el-button v-else type="info" >预约抢购</el-button>
							</div>
						</el-col>
					</el-row>
					
					
				</div>
			</el-col>
		</el-row>
		
		
	</div>
</template>

<script>
	import times from "./SecKillTime.vue"
	export default {
		name: "SecKillGoods",
		components:{
			times
		},
		data() {
			return {
				gid: this.$route.params.gid,
				title:"商品标题",
				info:"商品描述",
				price:"99.99",
				save:"100",
				overTime:"",
				flag : false
			}
		},
		methods: {
			getGoodsById() {
				var _this = this;
				this.utils.ajax({
					url: this.utils.urls.getGoodsById,
					data: {
						gid: this.gid
					},
					success: function(data) {
						_this.title = data.title;
						_this.info = data.info;
						_this.price = data.price;
						_this.save = data.save;
						_this.overTime = data.beginTime;
					}
				}, this);
			},
			overFunc(){
				this.flag = true;
			},
			qiangGou(){
				this.utils.ajax({
					url:this.utils.urls.kill,
					data:{
						gid:this.gid
					},
					success:function(data){
						
					}
				},this);
			}
		},
		mounted() {
			this.getGoodsById();
		}
	}
</script>

<style>
	.divclass{
		text-align: left;
		padding: 10px;
		margin-top: 20px;
	}
	.title {
		font-size: 30px;
		font-weight: 900;
	}
	.price{
		font-size: 20px;
		color: red;
	}
</style>
