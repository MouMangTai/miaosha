<template>
	<div>
		<h3>秒杀列表</h3>
		
		<el-row :gutter="20">
		  <el-col :span="4" :offset="2"><div class="grid-content bg-purple" @click="getGoodsList(times[0])">{{times[0]}}(进行中)</div></el-col>
		  <el-col :span="4"><div class="grid-content bg-purple" @click="getGoodsList(times[1])">{{times[1]}}(即将开始)</div></el-col>
		  <el-col :span="4"><div class="grid-content bg-purple" @click="getGoodsList(times[2])">{{times[2]}}(即将开始)</div></el-col>
		  <el-col :span="4"><div class="grid-content bg-purple" @click="getGoodsList(times[3])">{{times[3]}}(即将开始)</div></el-col>
			<el-col :span="4"><div class="grid-content bg-purple" @click="getGoodsList(times[4])">{{times[4]}}(即将开始)</div></el-col>
		</el-row>
		
		
		<el-row :gutter="20">
			<el-col :span="6" v-for="(good, index) in goods" :key="index" style="margin-top:10px;">
			    <el-card :body-style="{ padding: '0px' }" >
			      <img src="https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png" class="image" @click="showGoods(good.id)">
			      <div style="padding: 14px;">
			        <span>{{good.title}}</span>
			        <div class="bottom clearfix">
			          <span class="save">库存:{{ good.save }}</span>
								<span class="price">价格:￥{{ good.price }}</span>
			          <el-button type="text" class="button" @click="showGoods(good.id)">立即抢购</el-button>
			        </div>
			      </div>
			    </el-card>
			  </el-col>
			
			
		</el-row>
	</div>
</template>

<script>
	export default {
		methods: {
			getSecList(){
				var _this = this;
				this.utils.ajax({
					url:_this.utils.urls.getTimes,
					success:function(data){
						for(var i = 0;i < data.length; i++){
							_this.times.push(data[i]);
							
							if(i==0){
								_this.getGoodsList(data[i]);
							}
						}
					}
				},this);
			},
			getGoodsList(time){
				var _this = this;
				this.utils.ajax({
					url : _this.utils.urls.listByTime,
					data:{
						time:time
					},
					success:function(data){
						_this.goods = data;
					}
				},this);
			},
			showGoods(id){
				this.$router.push("/SecKillGoods/"+id);
			}
		},
		data() {
			return {
				times:[],
				goods:[]
			}
		},
		mounted(){
			this.getSecList();
		}
		
	}
</script>

<style>
	.el-row {
	    margin-bottom: 20px;
	    &:last-child {
	      margin-bottom: 0;
	    }
	  }
	  .el-col {
	    border-radius: 4px;
	  }
	  .bg-purple-dark {
	    background: #99a9bf;
	  }
	  .bg-purple {
	    background: #d3dce6;
	  }
	  .bg-purple-light {
	    background: #e5e9f2;
	  }
	  .grid-content {
			line-height: 40px;
	    border-radius: 4px;
	    min-height: 36px;
	  }
	  .row-bg {
	    padding: 10px 0;
	    background-color: #f9fafc;
	  }
		.save {
		    font-size: 13px;
		    color: #999;
		  }
			.price {
			    font-size: 20px;
			    color: #99180f;
			  }
		  
		  .bottom {
		    margin-top: 13px;
		    line-height: 12px;
		  }
		
		  .button {
		    padding: 0;
		    float: right;
		  }
		
		  .image {
		    width: 200px;
				height: 200px;
		    /* display: block; */
		  }
		
		  .clearfix:before,
		  .clearfix:after {
		      display: table;
		      content: "";
		  }
		  
		  .clearfix:after {
		      clear: both
		  }
</style>
