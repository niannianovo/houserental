/**
 * 省市区三级联动数据
 * 格式: [{ value, label, children: [{ value, label, children: [...] }] }]
 */
const areaData = [
  {
    value: '北京市', label: '北京市', children: [
      { value: '北京市', label: '北京市', children: [
        { value: '东城区', label: '东城区' }, { value: '西城区', label: '西城区' },
        { value: '朝阳区', label: '朝阳区' }, { value: '丰台区', label: '丰台区' },
        { value: '石景山区', label: '石景山区' }, { value: '海淀区', label: '海淀区' },
        { value: '门头沟区', label: '门头沟区' }, { value: '房山区', label: '房山区' },
        { value: '通州区', label: '通州区' }, { value: '顺义区', label: '顺义区' },
        { value: '昌平区', label: '昌平区' }, { value: '大兴区', label: '大兴区' },
        { value: '怀柔区', label: '怀柔区' }, { value: '平谷区', label: '平谷区' },
        { value: '密云区', label: '密云区' }, { value: '延庆区', label: '延庆区' }
      ]}
    ]
  },
  {
    value: '上海市', label: '上海市', children: [
      { value: '上海市', label: '上海市', children: [
        { value: '黄浦区', label: '黄浦区' }, { value: '徐汇区', label: '徐汇区' },
        { value: '长宁区', label: '长宁区' }, { value: '静安区', label: '静安区' },
        { value: '普陀区', label: '普陀区' }, { value: '虹口区', label: '虹口区' },
        { value: '杨浦区', label: '杨浦区' }, { value: '闵行区', label: '闵行区' },
        { value: '宝山区', label: '宝山区' }, { value: '嘉定区', label: '嘉定区' },
        { value: '浦东新区', label: '浦东新区' }, { value: '金山区', label: '金山区' },
        { value: '松江区', label: '松江区' }, { value: '青浦区', label: '青浦区' },
        { value: '奉贤区', label: '奉贤区' }, { value: '崇明区', label: '崇明区' }
      ]}
    ]
  },
  {
    value: '广东省', label: '广东省', children: [
      { value: '广州市', label: '广州市', children: [
        { value: '越秀区', label: '越秀区' }, { value: '海珠区', label: '海珠区' },
        { value: '荔湾区', label: '荔湾区' }, { value: '天河区', label: '天河区' },
        { value: '白云区', label: '白云区' }, { value: '黄埔区', label: '黄埔区' },
        { value: '番禺区', label: '番禺区' }, { value: '花都区', label: '花都区' },
        { value: '南沙区', label: '南沙区' }, { value: '从化区', label: '从化区' },
        { value: '增城区', label: '增城区' }
      ]},
      { value: '深圳市', label: '深圳市', children: [
        { value: '罗湖区', label: '罗湖区' }, { value: '福田区', label: '福田区' },
        { value: '南山区', label: '南山区' }, { value: '宝安区', label: '宝安区' },
        { value: '龙岗区', label: '龙岗区' }, { value: '盐田区', label: '盐田区' },
        { value: '龙华区', label: '龙华区' }, { value: '坪山区', label: '坪山区' },
        { value: '光明区', label: '光明区' }
      ]},
      { value: '珠海市', label: '珠海市', children: [
        { value: '香洲区', label: '香洲区' }, { value: '斗门区', label: '斗门区' }, { value: '金湾区', label: '金湾区' }
      ]},
      { value: '东莞市', label: '东莞市', children: [
        { value: '东城街道', label: '东城街道' }, { value: '南城街道', label: '南城街道' },
        { value: '万江街道', label: '万江街道' }, { value: '莞城街道', label: '莞城街道' }
      ]},
      { value: '佛山市', label: '佛山市', children: [
        { value: '禅城区', label: '禅城区' }, { value: '南海区', label: '南海区' },
        { value: '顺德区', label: '顺德区' }, { value: '三水区', label: '三水区' }, { value: '高明区', label: '高明区' }
      ]}
    ]
  },
  {
    value: '江苏省', label: '江苏省', children: [
      { value: '南京市', label: '南京市', children: [
        { value: '玄武区', label: '玄武区' }, { value: '秦淮区', label: '秦淮区' },
        { value: '建邺区', label: '建邺区' }, { value: '鼓楼区', label: '鼓楼区' },
        { value: '浦口区', label: '浦口区' }, { value: '栖霞区', label: '栖霞区' },
        { value: '雨花台区', label: '雨花台区' }, { value: '江宁区', label: '江宁区' }
      ]},
      { value: '苏州市', label: '苏州市', children: [
        { value: '虎丘区', label: '虎丘区' }, { value: '吴中区', label: '吴中区' },
        { value: '相城区', label: '相城区' }, { value: '姑苏区', label: '姑苏区' },
        { value: '吴江区', label: '吴江区' }, { value: '工业园区', label: '工业园区' }
      ]},
      { value: '无锡市', label: '无锡市', children: [
        { value: '锡山区', label: '锡山区' }, { value: '惠山区', label: '惠山区' },
        { value: '滨湖区', label: '滨湖区' }, { value: '梁溪区', label: '梁溪区' }, { value: '新吴区', label: '新吴区' }
      ]}
    ]
  },
  {
    value: '浙江省', label: '浙江省', children: [
      { value: '杭州市', label: '杭州市', children: [
        { value: '上城区', label: '上城区' }, { value: '拱墅区', label: '拱墅区' },
        { value: '西湖区', label: '西湖区' }, { value: '滨江区', label: '滨江区' },
        { value: '萧山区', label: '萧山区' }, { value: '余杭区', label: '余杭区' },
        { value: '临平区', label: '临平区' }, { value: '钱塘区', label: '钱塘区' }
      ]},
      { value: '宁波市', label: '宁波市', children: [
        { value: '海曙区', label: '海曙区' }, { value: '江北区', label: '江北区' },
        { value: '北仑区', label: '北仑区' }, { value: '鄞州区', label: '鄞州区' }
      ]},
      { value: '温州市', label: '温州市', children: [
        { value: '鹿城区', label: '鹿城区' }, { value: '龙湾区', label: '龙湾区' }, { value: '瓯海区', label: '瓯海区' }
      ]}
    ]
  },
  {
    value: '四川省', label: '四川省', children: [
      { value: '成都市', label: '成都市', children: [
        { value: '锦江区', label: '锦江区' }, { value: '青羊区', label: '青羊区' },
        { value: '金牛区', label: '金牛区' }, { value: '武侯区', label: '武侯区' },
        { value: '成华区', label: '成华区' }, { value: '龙泉驿区', label: '龙泉驿区' },
        { value: '青白江区', label: '青白江区' }, { value: '新都区', label: '新都区' },
        { value: '温江区', label: '温江区' }, { value: '双流区', label: '双流区' },
        { value: '郫都区', label: '郫都区' }, { value: '天府新区', label: '天府新区' },
        { value: '高新区', label: '高新区' }
      ]}
    ]
  },
  {
    value: '湖北省', label: '湖北省', children: [
      { value: '武汉市', label: '武汉市', children: [
        { value: '江岸区', label: '江岸区' }, { value: '江汉区', label: '江汉区' },
        { value: '硚口区', label: '硚口区' }, { value: '汉阳区', label: '汉阳区' },
        { value: '武昌区', label: '武昌区' }, { value: '青山区', label: '青山区' },
        { value: '洪山区', label: '洪山区' }, { value: '东西湖区', label: '东西湖区' }
      ]}
    ]
  },
  {
    value: '湖南省', label: '湖南省', children: [
      { value: '长沙市', label: '长沙市', children: [
        { value: '芙蓉区', label: '芙蓉区' }, { value: '天心区', label: '天心区' },
        { value: '岳麓区', label: '岳麓区' }, { value: '开福区', label: '开福区' },
        { value: '雨花区', label: '雨花区' }, { value: '望城区', label: '望城区' },
        { value: '长沙县', label: '长沙县' }, { value: '浏阳市', label: '浏阳市' },
        { value: '宁乡市', label: '宁乡市' }
      ]},
      { value: '株洲市', label: '株洲市', children: [
        { value: '天元区', label: '天元区' }, { value: '芦淞区', label: '芦淞区' },
        { value: '荷塘区', label: '荷塘区' }, { value: '石峰区', label: '石峰区' },
        { value: '渌口区', label: '渌口区' }, { value: '醴陵市', label: '醴陵市' }
      ]},
      { value: '湘潭市', label: '湘潭市', children: [
        { value: '雨湖区', label: '雨湖区' }, { value: '岳塘区', label: '岳塘区' },
        { value: '湘潭县', label: '湘潭县' }, { value: '湘乡市', label: '湘乡市' },
        { value: '韶山市', label: '韶山市' }
      ]},
      { value: '衡阳市', label: '衡阳市', children: [
        { value: '珠晖区', label: '珠晖区' }, { value: '雁峰区', label: '雁峰区' },
        { value: '石鼓区', label: '石鼓区' }, { value: '蒸湘区', label: '蒸湘区' },
        { value: '南岳区', label: '南岳区' }, { value: '衡阳县', label: '衡阳县' },
        { value: '衡南县', label: '衡南县' }, { value: '耒阳市', label: '耒阳市' },
        { value: '常宁市', label: '常宁市' }
      ]},
      { value: '邵阳市', label: '邵阳市', children: [
        { value: '双清区', label: '双清区' }, { value: '大祥区', label: '大祥区' },
        { value: '北塔区', label: '北塔区' }, { value: '邵东市', label: '邵东市' },
        { value: '新邵县', label: '新邵县' }, { value: '隆回县', label: '隆回县' },
        { value: '武冈市', label: '武冈市' }
      ]},
      { value: '岳阳市', label: '岳阳市', children: [
        { value: '岳阳楼区', label: '岳阳楼区' }, { value: '云溪区', label: '云溪区' },
        { value: '君山区', label: '君山区' }, { value: '岳阳县', label: '岳阳县' },
        { value: '汨罗市', label: '汨罗市' }, { value: '临湘市', label: '临湘市' }
      ]},
      { value: '常德市', label: '常德市', children: [
        { value: '武陵区', label: '武陵区' }, { value: '鼎城区', label: '鼎城区' },
        { value: '津市市', label: '津市市' }, { value: '澧县', label: '澧县' },
        { value: '桃源县', label: '桃源县' }, { value: '汉寿县', label: '汉寿县' }
      ]},
      { value: '张家界市', label: '张家界市', children: [
        { value: '永定区', label: '永定区' }, { value: '武陵源区', label: '武陵源区' },
        { value: '慈利县', label: '慈利县' }, { value: '桑植县', label: '桑植县' }
      ]},
      { value: '益阳市', label: '益阳市', children: [
        { value: '资阳区', label: '资阳区' }, { value: '赫山区', label: '赫山区' },
        { value: '沅江市', label: '沅江市' }, { value: '桃江县', label: '桃江县' },
        { value: '安化县', label: '安化县' }
      ]},
      { value: '郴州市', label: '郴州市', children: [
        { value: '北湖区', label: '北湖区' }, { value: '苏仙区', label: '苏仙区' },
        { value: '桂阳县', label: '桂阳县' }, { value: '永兴县', label: '永兴县' },
        { value: '宜章县', label: '宜章县' }, { value: '资兴市', label: '资兴市' }
      ]},
      { value: '永州市', label: '永州市', children: [
        { value: '零陵区', label: '零陵区' }, { value: '冷水滩区', label: '冷水滩区' },
        { value: '祁阳市', label: '祁阳市' }, { value: '东安县', label: '东安县' },
        { value: '道县', label: '道县' }, { value: '宁远县', label: '宁远县' }
      ]},
      { value: '怀化市', label: '怀化市', children: [
        { value: '鹤城区', label: '鹤城区' }, { value: '洪江市', label: '洪江市' },
        { value: '沅陵县', label: '沅陵县' }, { value: '溆浦县', label: '溆浦县' },
        { value: '会同县', label: '会同县' }
      ]},
      { value: '娄底市', label: '娄底市', children: [
        { value: '娄星区', label: '娄星区' }, { value: '涟源市', label: '涟源市' },
        { value: '双峰县', label: '双峰县' }, { value: '新化县', label: '新化县' },
        { value: '冷水江市', label: '冷水江市' }
      ]},
      { value: '湘西土家族苗族自治州', label: '湘西土家族苗族自治州', children: [
        { value: '吉首市', label: '吉首市' }, { value: '泸溪县', label: '泸溪县' },
        { value: '凤凰县', label: '凤凰县' }, { value: '花垣县', label: '花垣县' },
        { value: '保靖县', label: '保靖县' }, { value: '永顺县', label: '永顺县' },
        { value: '龙山县', label: '龙山县' }, { value: '古丈县', label: '古丈县' }
      ]}
    ]
  },
  {
    value: '天津市', label: '天津市', children: [
      { value: '天津市', label: '天津市', children: [
        { value: '和平区', label: '和平区' }, { value: '河东区', label: '河东区' },
        { value: '河西区', label: '河西区' }, { value: '南开区', label: '南开区' },
        { value: '河北区', label: '河北区' }, { value: '红桥区', label: '红桥区' },
        { value: '滨海新区', label: '滨海新区' }, { value: '东丽区', label: '东丽区' },
        { value: '西青区', label: '西青区' }, { value: '津南区', label: '津南区' },
        { value: '北辰区', label: '北辰区' }, { value: '武清区', label: '武清区' }
      ]}
    ]
  },
  {
    value: '重庆市', label: '重庆市', children: [
      { value: '重庆市', label: '重庆市', children: [
        { value: '渝中区', label: '渝中区' }, { value: '大渡口区', label: '大渡口区' },
        { value: '江北区', label: '江北区' }, { value: '沙坪坝区', label: '沙坪坝区' },
        { value: '九龙坡区', label: '九龙坡区' }, { value: '南岸区', label: '南岸区' },
        { value: '北碚区', label: '北碚区' }, { value: '渝北区', label: '渝北区' },
        { value: '巴南区', label: '巴南区' }
      ]}
    ]
  },
  {
    value: '福建省', label: '福建省', children: [
      { value: '福州市', label: '福州市', children: [
        { value: '鼓楼区', label: '鼓楼区' }, { value: '台江区', label: '台江区' },
        { value: '仓山区', label: '仓山区' }, { value: '马尾区', label: '马尾区' },
        { value: '晋安区', label: '晋安区' }
      ]},
      { value: '厦门市', label: '厦门市', children: [
        { value: '思明区', label: '思明区' }, { value: '海沧区', label: '海沧区' },
        { value: '湖里区', label: '湖里区' }, { value: '集美区', label: '集美区' },
        { value: '同安区', label: '同安区' }, { value: '翔安区', label: '翔安区' }
      ]}
    ]
  },
  {
    value: '山东省', label: '山东省', children: [
      { value: '济南市', label: '济南市', children: [
        { value: '历下区', label: '历下区' }, { value: '市中区', label: '市中区' },
        { value: '槐荫区', label: '槐荫区' }, { value: '天桥区', label: '天桥区' },
        { value: '历城区', label: '历城区' }
      ]},
      { value: '青岛市', label: '青岛市', children: [
        { value: '市南区', label: '市南区' }, { value: '市北区', label: '市北区' },
        { value: '黄岛区', label: '黄岛区' }, { value: '崂山区', label: '崂山区' },
        { value: '李沧区', label: '李沧区' }, { value: '城阳区', label: '城阳区' }
      ]}
    ]
  },
  {
    value: '河南省', label: '河南省', children: [
      { value: '郑州市', label: '郑州市', children: [
        { value: '中原区', label: '中原区' }, { value: '二七区', label: '二七区' },
        { value: '管城回族区', label: '管城回族区' }, { value: '金水区', label: '金水区' },
        { value: '上街区', label: '上街区' }, { value: '惠济区', label: '惠济区' }
      ]}
    ]
  },
  {
    value: '陕西省', label: '陕西省', children: [
      { value: '西安市', label: '西安市', children: [
        { value: '新城区', label: '新城区' }, { value: '碑林区', label: '碑林区' },
        { value: '莲湖区', label: '莲湖区' }, { value: '灞桥区', label: '灞桥区' },
        { value: '未央区', label: '未央区' }, { value: '雁塔区', label: '雁塔区' },
        { value: '长安区', label: '长安区' }, { value: '高新区', label: '高新区' }
      ]}
    ]
  },
  {
    value: '辽宁省', label: '辽宁省', children: [
      { value: '沈阳市', label: '沈阳市', children: [
        { value: '和平区', label: '和平区' }, { value: '沈河区', label: '沈河区' },
        { value: '大东区', label: '大东区' }, { value: '皇姑区', label: '皇姑区' },
        { value: '铁西区', label: '铁西区' }, { value: '浑南区', label: '浑南区' }
      ]},
      { value: '大连市', label: '大连市', children: [
        { value: '中山区', label: '中山区' }, { value: '西岗区', label: '西岗区' },
        { value: '沙河口区', label: '沙河口区' }, { value: '甘井子区', label: '甘井子区' }
      ]}
    ]
  }
]

export default areaData
