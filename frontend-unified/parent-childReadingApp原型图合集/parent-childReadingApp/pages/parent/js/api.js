// 模拟API服务
class FamilyAnalysisAPI {
    constructor() {
        this.baseData = {
            familyMembers: [
                { id: "dad", name: "爸爸", group: 1, avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=dad123&backgroundColor=b6e3f4" },
                { id: "mom", name: "妈妈", group: 1, avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=mom456" },
                { id: "son", name: "小明", group: 2, avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=xiaoming345" },
                { id: "grandpa", name: "爷爷", group: 3, avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=grandpa789" },
                { id: "grandma", name: "奶奶", group: 3, avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=grandma012" }
            ],
            relationships: [
                { source: "dad", target: "mom", value: 0.9, interactions: 15 },
                { source: "dad", target: "son", value: 0.95, interactions: 25 },
                { source: "mom", target: "son", value: 0.98, interactions: 28 },
                { source: "grandpa", target: "dad", value: 0.88, interactions: 10 },
                { source: "grandma", target: "dad", value: 0.85, interactions: 8 },
                { source: "grandpa", target: "grandma", value: 0.92, interactions: 20 }
            ]
        };
    }

    // 获取家庭成员数据
    async getFamilyMembers() {
        return new Promise(resolve => {
            setTimeout(() => {
                resolve(this.baseData.familyMembers);
            }, 300);
        });
    }

    // 获取关系数据
    async getRelationships() {
        return new Promise(resolve => {
            setTimeout(() => {
                resolve(this.baseData.relationships);
            }, 300);
        });
    }

    // 获取情感分析数据
    async getEmotionData(timeRange = 'week') {
        const ranges = {
            week: {
                labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
                data: this._generateRandomData(7)
            },
            month: {
                labels: ['第1周', '第2周', '第3周', '第4周'],
                data: this._generateRandomData(4)
            },
            year: {
                labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
                data: this._generateRandomData(6)
            }
        };

        return new Promise(resolve => {
            setTimeout(() => {
                resolve(ranges[timeRange]);
            }, 300);
        });
    }

    // 获取互动记录
    async getInteractionRecords(page = 1, limit = 5) {
        const records = [
            {
                id: 1,
                type: 'reading',
                title: '共读《小王子》',
                description: '和爸爸一起读完了第三章，讨论了小王子遇到狐狸的故事。',
                time: '2小时前',
                emotion: '积极',
                interaction: '良好'
            },
            {
                id: 2,
                type: 'chat',
                title: '晚餐交流',
                description: '和妈妈讨论了今天学校发生的趣事。',
                time: '5小时前',
                emotion: '愉快',
                interaction: '积极'
            },
            {
                id: 3,
                type: 'activity',
                title: '和爷爷下棋',
                description: '学习了新的下棋技巧，爷爷很有耐心地教导。',
                time: '昨天',
                emotion: '专注',
                interaction: '热烈'
            }
        ];

        return new Promise(resolve => {
            setTimeout(() => {
                const start = (page - 1) * limit;
                const end = start + limit;
                resolve({
                    records: records.slice(start, end),
                    total: records.length,
                    page,
                    limit
                });
            }, 300);
        });
    }

    // 获取总体统计数据
    async getStatistics() {
        return new Promise(resolve => {
            setTimeout(() => {
                resolve({
                    intimacyRate: 98,
                    weeklyInteractions: 12,
                    positiveRate: 85,
                    focusRate: 78,
                    interactionRate: 92
                });
            }, 300);
        });
    }

    // 获取关系网络数据
    async getRelationshipNetwork() {
        return new Promise(resolve => {
            setTimeout(() => {
                resolve({
                    nodes: this.baseData.familyMembers.map(member => ({
                        ...member,
                        intimacy: Math.round(Math.random() * 20 + 80), // 80-100的随机值
                        weeklyInteractions: Math.round(Math.random() * 15 + 10) // 10-25的随机值
                    })),
                    links: this.baseData.relationships
                });
            }, 300);
        });
    }

    // 生成随机数据辅助函数
    _generateRandomData(length) {
        return {
            positive: Array.from({length}, () => 0.75 + Math.random() * 0.2),
            focus: Array.from({length}, () => 0.7 + Math.random() * 0.2),
            interaction: Array.from({length}, () => 0.85 + Math.random() * 0.1)
        };
    }
}

// 导出API实例
window.familyAPI = new FamilyAnalysisAPI(); 