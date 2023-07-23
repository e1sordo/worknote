<template>
  <div class="home">
    <div class="m-4 text-start">
      <h2>{{ $t("analytics.timeAllocation") }}</h2>
      <apexchart width="100%" height="400" type="bar" :options="chart1Options" :series="chart1Series"></apexchart>
    </div>
    <div class="m-4 text-start">
      <h2>{{ $t("analytics.mostTimeConsumingTasks") }}</h2>
      <apexchart width="100%" height="400" type="bar" :options="chart2.options" :series="chart2.series"></apexchart>
    </div>
  </div>
</template>

<script lang="ts">
import api, { TimeDistributionDto } from "@/api/backend-api";
import { defineComponent } from 'vue';

interface OutputData {
  name: string;
  data: number[];
}

export default defineComponent({
  name: 'AnalyticsView',

  data() {
    return {
      chart1Series: [{
        name: 'test',
        data: [0]
      }],
      chart1Options: {},

      chart2: {
        options: {
          chart: {
            id: 'vuechart-example'
          },
          xaxis: {
            categories: [1991, 1992, 1993, 1994, 1995, 1996, 1997, 1998]
          }
        },
        series: [{
          name: 'series-1',
          data: [30, 40, 45, 50, 49, 60, 70, 91]
        }]
      }
    };
  },
  methods: {
    fetchDataForChart1() {
      api.getTimeDistributionByTypeOfTasksPerWeeks()
        .then(response => {
          const chartData = this.transformData(response.data.data);
          this.renderChart(chartData);
        })
        .catch(error => {
          console.error('Ошибка при загрузке данных:', error);
        });
    },
    transformData(input: TimeDistributionDto[]): [OutputData[], string[]] {
      const names: string[] = [];
      const data: number[][] = [];
      const periods: string[] = [];

      for (const item of input) {
        for (const [name, value] of Object.entries(item.byTypes)) {
          const index = names.indexOf(name);
          if (index === -1) {
            names.push(name);
            data.push([value / 60]);
          } else {
            data[index].push(value / 60);
          }
        }
        periods.push(item.periodEnd);
      }

      const outputData: OutputData[] = names.map((name, index) => {
        return {
          name: name,
          data: data[index],
        };
      });

      return [outputData, periods];
    },
    renderChart(chartData: [OutputData[], string[]]) {
      const options = {
        theme: {
          mode: 'light',
          palette: 'palette7',
        },
        chart: {
          type: 'bar',
          height: 350,
          width: '100%',
          stacked: true
        },
        responsive: [{
          breakpoint: 480,
          options: {
            legend: {
              position: 'bottom',
              offsetX: -10,
              offsetY: 0
            }
          }
        }],
        stroke: {
          width: 1,
          colors: ['#fff']
        },
        xaxis: {
          // type: 'datetime',
          categories: chartData[1],
        },
        yaxis: {
          max: 40,
          decimalsInFloat: 0
        },
        fill: {
          opacity: 1,
          // type: "gradient",
          // gradient: {
          //   shade: "dark",
          //   type: "vertical",
          //   shadeIntensity: 0.35,
          //   gradientToColors: undefined,
          //   inverseColors: false,
          //   opacityFrom: 0.85,
          //   opacityTo: 0.85,
          //   stops: [90, 0, 100]
          // }
        },
        legend: {
          position: 'right',
          offsetX: 0,
          offsetY: 50
        },
        dataLabels: {
          dropShadow: {
            enabled: true
          },
          formatter: function (val: number) {
            return val.toFixed(2) + 'ч';
          }
        }
      }

      this.chart1Options = options;
      this.chart1Series = chartData[0];
    }
  },
  mounted() {
    this.fetchDataForChart1();
  }
});
</script>