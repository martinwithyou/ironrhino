<map map_file="maps/china.swf" tl_long="73.620045" tl_lat="53.553745" br_long="134.768463" br_lat="18.168882" zoom="95%" zoom_x="3.63%" zoom_y="2.68%"> 
    <areas>
    <#list areas?values as area>
        <area mc_name="${area.mcName!}" title="${area.title!}:${area.value!}" color="${area.color!}"></area>
        </#list>
    </areas> 
    <movies> 
        <movie title="${movies['CN_11']!}<#if areas['CN_11']??>:${areas['CN_11'].value}</#if>" file="target" color="#CC0000" width="15" height="15" long="116.387909" lat="39.90601" fixed_size="true"></movie> 
        <movie title="${movies['CN_91']!}<#if areas['CN_91']??>:${areas['CN_91'].value}</#if>" file="circle" color="#000000" width="5" height="5" long="114.153542" lat="22.411249" fixed_size="true"></movie> 
        <movie title="${movies['CN_92']!}<#if areas['CN_92']??>:${areas['CN_92'].value}</#if>" file="circle" color="#000000" width="5" height="5" long="113.5502" lat="22.1636915147872" fixed_size="true"></movie> 
    </movies> 
    <labels> 
        <label x="38.3693%" y="26.6968%" text_size="18" color="#000000" remain="false"> 
            <text>${label!}</text> 
        </label> 
    </labels> 
</map>